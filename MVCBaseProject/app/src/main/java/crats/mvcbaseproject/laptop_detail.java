package crats.mvcbaseproject;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import crats.mvcbaseproject.Database.Database;
import crats.mvcbaseproject.controller.Common;
import crats.mvcbaseproject.model.Brand;
import crats.mvcbaseproject.model.Order;

public class laptop_detail extends AppCompatActivity {

    TextView laptop_name,laptop_price,laptop_description;
    ImageView laptop_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;

    String laptopId="";
    FirebaseDatabase database;
    DatabaseReference foods;

    Brand currentBrand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laptop_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Laptops");

        //Init view
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        laptopId,
                        currentBrand.getName(),
                        numberButton.getNumber(),
                        currentBrand.getPrice(),
                        currentBrand.getDiscount()

                ));

                Toast.makeText(laptop_detail.this,"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });

        laptop_description = (TextView)findViewById(R.id.laptop_description);
        laptop_name = (TextView)findViewById(R.id.laptop_name);
        laptop_price = (TextView)findViewById(R.id.laptop_price);
        laptop_image=(ImageView)findViewById(R.id.img_food);

        //get Food Id From Intent

        if(getIntent() != null)
            laptopId = getIntent().getStringExtra("laptopID");
        if(!laptopId.isEmpty())
        {
            if(Common.isConnectedToInternet(getBaseContext())){
                getDetailFood(laptopId);
            }
            else{
                Toast.makeText(laptop_detail.this,"Check Your Connection",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getDetailFood(String laptopId){
        foods.child(laptopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentBrand = dataSnapshot.getValue(Brand.class);

                //set Image
                Picasso.with(getBaseContext()).load(currentBrand.getImage()).into(laptop_image);

                //collapsingToolbarLayout.setTitle(currentBrand.getName());
                laptop_price.setText(currentBrand.getPrice());
                laptop_name.setText(currentBrand.getName());
                laptop_description.setText(currentBrand.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

