package com.mybooks.mybooks.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mybooks.mybooks.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderDetailsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    FirebaseRecyclerAdapter<OrderDetailsBookList, OrderDetailsViewHolder> firebaseRecyclerAdapter;

    String orderId;
    String orderType;

    List<OrderDetailsActivity> orderDetailsActivityList;

    private LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        recyclerView = (RecyclerView) findViewById(R.id.orderDetailsRecycleView);
        recyclerView.setNestedScrollingEnabled(false);
        //recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Bundle bundle = getIntent().getExtras();
        orderId = bundle.getString("orderId");
        orderType = bundle.getString("orderType");

        setToolbar();

        setShippingAddress();

        setProductDetails();
    }

    private void setShippingAddress() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Order")
                .child(orderId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String address = String.valueOf(dataSnapshot.child("deliveryaddress").getValue());
                TextView add = (TextView) findViewById(R.id.address);
                add.setText(address.replaceFirst(", ", "\n"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Orders ID: " + getOrderId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public String getOrderId() {
        int len = orderId.length();
        int setLen = 10 - len;
        String final_order_id = "0000000000";
        final_order_id = orderType + final_order_id.substring(0, setLen) + orderId;
        return  final_order_id;
    }

    public void setProductDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("OrderDetails").child(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString().replace(".", "*")).child(orderId);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<OrderDetailsBookList, OrderDetailsViewHolder>(
                OrderDetailsBookList.class,
                R.layout.order_book_list_view,
                OrderDetailsViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(OrderDetailsViewHolder viewHolder, OrderDetailsBookList model, int position) {
                viewHolder.setFields(model.getKey(), model.getBooktype(), model.getQuantity(), model.getPrice());
            }
        };

        mLayoutManager = new LinearLayoutManager(OrderDetailsActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class OrderDetailsViewHolder extends RecyclerView.ViewHolder {
        View view;
        String key;

        public OrderDetailsViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadBookDetails(v.getContext(), key);
                }
            });
        }

        public void setFields(String key, String type, String qty, String price_per_book) {
            this.key = key;
            final TextView mTitle = (TextView) view.findViewById(R.id.obookTitle);
            final TextView mPublisher = (TextView) view.findViewById(R.id.obookPublisher);
            final TextView mAuthor = (TextView) view.findViewById(R.id.obookAuthor);
            TextView mType = (TextView) view.findViewById(R.id.obooktype);
            TextView mPricePerBook = (TextView) view.findViewById(R.id.obookPricePerBook);
            TextView mQty = (TextView) view.findViewById(R.id.oQuantity);
            TextView mbook_grand = (TextView) view.findViewById(R.id.obookGrandTotal);
            final ImageView mBookImage = (ImageView) view.findViewById(R.id.obookImage);


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Products").child(key);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mTitle.setText(capitalizeEveryWord(dataSnapshot.child("f2").getValue().toString()));
                    mPublisher.setText(capitalizeEveryWord(dataSnapshot.child("f3").getValue().toString()));
                    mPublisher.setVisibility(View.GONE);
                    mAuthor.setText(capitalizeEveryWord(dataSnapshot.child("f4").getValue().toString()));
                    mAuthor.setVisibility(View.GONE);

                    if (!dataSnapshot.child("f13").getValue().toString().equalsIgnoreCase("na")) {
                        Glide.with(view.getContext())
                                .load(dataSnapshot.child("f13").getValue().toString())
                                .into(mBookImage);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mType.setText("Book Type: " + capitalizeEveryWord(type));
            mPricePerBook.setText("Price per Book: \u20B9 " + price_per_book);
            mQty.setText("Quantity: " + qty);
            int grand = Integer.parseInt(price_per_book) * Integer.parseInt(qty);
            mbook_grand.setText("Total Price: \u20B9 " + grand);
        }


        public String capitalizeEveryWord(String str) {

            if (str == null)
                return "";

            System.out.println(str);
            StringBuffer stringbf = new StringBuffer();
            Matcher m = Pattern.compile(
                    "([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(str);

            while (m.find()) {
                m.appendReplacement(
                        stringbf, m.group(1).toUpperCase() + m.group(2).toLowerCase());
            }
            return m.appendTail(stringbf).toString();
        }

        public void loadBookDetails(Context ctx, String key) {
            Intent intent = new Intent(ctx, Individual_book_details.class);
            intent.putExtra("key", key);
            ctx.startActivity(intent);
        }
    }
}


