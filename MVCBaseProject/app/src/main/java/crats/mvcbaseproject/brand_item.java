package crats.mvcbaseproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import crats.mvcbaseproject.controller.Common;
import crats.mvcbaseproject.controller.ItemClickListener;
import crats.mvcbaseproject.model.Brand;
import crats.mvcbaseproject.view.BrandViewHolder;

public class brand_item extends AppCompatActivity {

    //First Page After Category Selected From Brand to Computers

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;

    RelativeLayout rootLayout;

    //Fire base
    FirebaseDatabase db;
    DatabaseReference brand_item;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";

    FirebaseRecyclerAdapter<Brand,BrandViewHolder> adapter;

    //Add new Brand
    EditText edtName,edtDescription,edtPrice,edtDiscount;
    Button btnSelect, btnUpload;

    Brand newBrand;
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_item);

        //FireBase
        db = FirebaseDatabase.getInstance();
        brand_item = db.getReference("Laptops");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Later
                showAddFoodDialog();
            }
        });

        if(getIntent() != null){
            categoryId = getIntent().getStringExtra("CategoryId");
            //categoryId = "01";
        }
        if(!categoryId.isEmpty()){
            loadListFood(categoryId);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        brand_item.child(key).removeValue();
    }

    private void showUpdateFoodDialog(final String key, final Brand item) {
        AlertDialog.Builder alertDialog =  new AlertDialog.Builder(brand_item.this);
        alertDialog.setTitle("Edit Food");
        alertDialog.setMessage("Please fill All Info");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_brand,null);

        edtName = (EditText) add_menu_layout.findViewById(R.id.edtName);
        edtDescription = (EditText) add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = (EditText) add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = (EditText) add_menu_layout.findViewById(R.id.edtDiscount);

        //Set Default value for View
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtPrice.setText(item.getPrice());
        edtDiscount.setText(item.getDiscount());

        btnSelect = (Button) add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = (Button) add_menu_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Update Info
                item.setName(edtName.getText().toString());
                item.setDescription(edtDescription.getText().toString());
                item.setPrice(edtPrice.getText().toString());
                item.setDiscount(edtDiscount.getText().toString());

                brand_item.child(key).setValue(item);
                //Snackbar.make(rootLayout," Category " + newcategory.getName() + "Was Added",Snackbar.LENGTH_SHORT)
                //       .show();
                Toast.makeText(brand_item.this," Category Added"+ item.getName() +" Was Edited ",Toast.LENGTH_SHORT).show();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Brand item) {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Uploading");
        mDialog.show();

        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("images/"+imageName);

        imageFolder.putFile(saveUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        Toast.makeText(brand_item.this,"Uploaded",Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Set Value For New Category
                                item.setImage(uri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Toast.makeText(brand_item.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDialog.setMessage("Uploded "+ progress + "%");
                    }
                });
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder alertDialog =  new AlertDialog.Builder(brand_item.this);
        alertDialog.setTitle("Add New Food");
        alertDialog.setMessage("Please fill All Info");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_brand,null);

        edtName = (EditText) add_menu_layout.findViewById(R.id.edtName);
        edtDescription = (EditText) add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = (EditText) add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = (EditText) add_menu_layout.findViewById(R.id.edtDiscount);

        btnSelect = (Button) add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = (Button) add_menu_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newBrand != null)
                {
                    brand_item.push().setValue(newBrand);
                    //Snackbar.make(rootLayout," New Category " + newcategory.getName() + "Was Added",Snackbar.LENGTH_SHORT)
                    //       .show();
                    Toast.makeText(brand_item.this,"New Category Added"+ newBrand.getName() +" Added ",Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void uploadImage() {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Uploading");
        mDialog.show();

        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("images/"+imageName);

        imageFolder.putFile(saveUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        Toast.makeText(brand_item.this,"Uploaded",Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Set Value For New Category
                                newBrand = new Brand();
                                newBrand.setName(edtName.getText().toString());
                                newBrand.setDescription(edtDescription.getText().toString());
                                newBrand.setPrice(edtPrice.getText().toString());
                                newBrand.setDiscount(edtDiscount.getText().toString());
                                newBrand.setMenuId(categoryId);
                                newBrand.setImage(uri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Toast.makeText(brand_item.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDialog.setMessage("Uploded "+ progress + "%");
                    }
                });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);
    }


    private void loadListFood(String categoryid) {
        adapter = new FirebaseRecyclerAdapter<Brand, BrandViewHolder>(
                Brand.class,
                R.layout.brand,
                BrandViewHolder.class,
                brand_item.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(BrandViewHolder viewHolder, Brand model, int position) {
                viewHolder.brand_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.brand_image);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean IsLongClick) {
                        //Later
                        Intent laptopDetail = new Intent(brand_item.this,laptop_detail.class);
                        laptopDetail.putExtra("laptopID",adapter.getRef(position).getKey());
                        startActivity(laptopDetail);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            saveUri = data.getData();
            btnSelect.setText("Selected");
        }
    }
}
