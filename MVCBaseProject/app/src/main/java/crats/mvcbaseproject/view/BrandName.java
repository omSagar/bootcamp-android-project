package crats.mvcbaseproject.view;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import crats.mvcbaseproject.R;
import crats.mvcbaseproject.controller.Common;
import crats.mvcbaseproject.controller.ItemClickListener;

/**
 * Created by OmSagar on 12/12/2017.
 */

public class BrandName extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener
        {
            // Related with Brand Home
            public TextView txtMenuName;
            public ImageView imageView;

            private ItemClickListener itemClickListener;

            public BrandName(View itemView) {
                super(itemView);

                txtMenuName = (TextView) itemView.findViewById(R.id.menu_name);
                imageView = (ImageView) itemView.findViewById(R.id.menu_image);

                itemView.setOnCreateContextMenuListener(this);
                itemView.setOnClickListener(this);
            }

            public void setItemClickListener(ItemClickListener itemClickListener) {
                this.itemClickListener = itemClickListener;
            }

            @Override
            public void onClick(View v) {
                itemClickListener.onClick(v, getAdapterPosition(), false);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("Select Action");

                menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
                menu.add(0, 1, getAdapterPosition(), Common.DELETE);

            }
        }

