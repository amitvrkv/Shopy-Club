package com.mybooks.mybooks;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BooksListPageNew extends AppCompatActivity implements View.OnClickListener {

    List<ModelProductList> listObjects;

    DatabaseReference databaseReference;
    RecyclerView recyclerView;

    //Filter views
    Button doneFilter;
    View filterLayout;
    Spinner spinnerCat1, spinnerCat2, spinnerCat3, spinnerCat4;
    List<String> list1;
    List<String> list2;
    List<String> list3;
    List<String> list4;

    FloatingActionButton floatingActionButtonFilter;

    //Search
    EditText editTextSearchData;

    TextView cart_item_count;
    ImageView my_cart_icon;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_list_page_new);
        setToolbar();

        act = this;

        cart_item_count = (TextView) findViewById(R.id.cart_item_count);
        my_cart_icon = (ImageView) findViewById(R.id.my_cart_icon);
        my_cart_icon.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        floatingActionButtonFilter = (FloatingActionButton) findViewById(R.id.floatingActionButtonFilter);
        floatingActionButtonFilter.setOnClickListener(this);

        editTextSearchData = (EditText) findViewById(R.id.search_data);
        editTextSearchData.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    editTextSearchData.setCursorVisible(false);
                    searchProduct();
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
        editTextSearchData.setOnClickListener(this);

        /*Filter initial settings settings*/
        spinnerCat1 = (Spinner) findViewById(R.id.cat1);
        spinnerCat2 = (Spinner) findViewById(R.id.cat2);
        spinnerCat2.setVisibility(View.GONE);
        spinnerCat3 = (Spinner) findViewById(R.id.cat3);
        spinnerCat3.setVisibility(View.GONE);
        spinnerCat4 = (Spinner) findViewById(R.id.cat4);
        spinnerCat4.setVisibility(View.GONE);
        filterLayout = findViewById(R.id.filterLayout);
        filterLayout.setVisibility(View.INVISIBLE);
        doneFilter = (Button) findViewById(R.id.doneFilter);
        doneFilter.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setFilter();
        setCartCount();

        Bundle bundle = getIntent().getExtras();
        String key = bundle.getString("f");
        if (key.equals("all")) {
            setDataOnPageStart();
        } else if (key.equals("wishlist")) {
            productByWishlist();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_order_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorite:
                productByWishlist();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        editTextSearchData.setCursorVisible(false);
        getSupportActionBar().setTitle("Order Books");
        switch (v.getId()) {
            case R.id.search_data:
                editTextSearchData.setCursorVisible(true);
                break;
            case R.id.my_cart_icon:
                startActivity(new Intent(this, MyCartNew.class));
                break;
            case R.id.floatingActionButtonFilter:
                setFloatingActionButtonFilter();
                break;

            case R.id.doneFilter:
                setDoneFilter();
                break;
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void setDoneFilter() {
        editTextSearchData.setText("");

        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_filter);
        filterLayout.startAnimation(slideUp);
        filterLayout.setVisibility(View.GONE);
        Animation fade_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        recyclerView.startAnimation(fade_in);
        recyclerView.setVisibility(View.VISIBLE);

        //setDataOnPageStart();
        Animation sd = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        floatingActionButtonFilter.startAnimation(sd);
        floatingActionButtonFilter.setVisibility(View.VISIBLE);


        if (spinnerCat1.getSelectedItem().equals("Category")) {
            setDataOnPageStart();
        } else if (spinnerCat2.getSelectedItem().equals("select")) {
            //Toast.makeText(getApplicationContext(),"> 1" , Toast.LENGTH_SHORT).show();
            productByCat1();
        } else if (spinnerCat3.getSelectedItem().equals("select")) {
            //Toast.makeText(getApplicationContext(),"> 2" , Toast.LENGTH_SHORT).show();
            productByCat2();
        } else if (spinnerCat4.getSelectedItem().equals("select")) {
            //Toast.makeText(getApplicationContext(),"> 3\n" +  modelProductList.getF1() + "\n" +  modelProductList.getF5() , Toast.LENGTH_SHORT).show();
            productByCat3();
        } else {
            //Toast.makeText(getApplicationContext(),"> 4" , Toast.LENGTH_SHORT).show();
            productByCat4();
        }
    }

    public void setFloatingActionButtonFilter() {
        Animation fade_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        recyclerView.startAnimation(fade_out);
        recyclerView.setVisibility(View.GONE);
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_filter);
        filterLayout.setAnimation(slideDown);
        filterLayout.setVisibility(View.VISIBLE);

        Animation sd = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        floatingActionButtonFilter.startAnimation(sd);
        floatingActionButtonFilter.setVisibility(View.GONE);
    }

    public void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Order Books");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(getString(R.string.database_path), null);
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS P_CART");
                Toast.makeText(getApplicationContext(), "Table deleted", Toast.LENGTH_SHORT).show();
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS WISHLIST");
            }
        });
    }

    public void setDataOnPageStart() {
        listObjects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Products");
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listObjects.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelProductList modelProductList = dataSnapshot1.getValue(ModelProductList.class);
                    listObjects.add(modelProductList);
                }
                RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), listObjects, cart_item_count, act);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerAdapterProductView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void productByCat1() {
        listObjects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listObjects.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelProductList modelProductList = dataSnapshot1.getValue(ModelProductList.class);
                    if (modelProductList.getF1().equals(spinnerCat1.getSelectedItem().toString())) {
                        listObjects.add(modelProductList);
                    }
                }
                RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), listObjects, cart_item_count, act);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerAdapterProductView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void productByCat2() {
        listObjects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listObjects.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelProductList modelProductList = dataSnapshot1.getValue(ModelProductList.class);
                    if (list3.contains(modelProductList.getF5())) {
                        listObjects.add(modelProductList);
                    }
                }
                RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), listObjects, cart_item_count, act);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerAdapterProductView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void productByCat3() {
        listObjects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listObjects.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelProductList modelProductList = dataSnapshot1.getValue(ModelProductList.class);
                    if (modelProductList.getF1().equals(spinnerCat1.getSelectedItem().toString()) && modelProductList.getF5().equals(spinnerCat3.getSelectedItem().toString())) {
                        listObjects.add(modelProductList);
                    }
                }
                RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), listObjects, cart_item_count, act);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerAdapterProductView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void productByCat4() {
        listObjects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("ProductbyCourse")
                .child(spinnerCat1.getSelectedItem().toString()).child(spinnerCat2.getSelectedItem().toString()).child(spinnerCat3.getSelectedItem().toString()).child(spinnerCat4.getSelectedItem().toString());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listObjects.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String key = dataSnapshot1.getKey();
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Products").child(key);
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ModelProductList modelProductList = dataSnapshot.getValue(ModelProductList.class);
                            listObjects.add(modelProductList);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), listObjects, cart_item_count, act);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerAdapterProductView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void productByWishlist() {
        getSupportActionBar().setTitle("Wish list");
        listObjects = new ArrayList<>();
        listObjects.clear();
        final int[] c = {0};
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(getString(R.string.database_path), null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WISHLIST(key VARCHAR);");
        final Cursor cursor = sqLiteDatabase.rawQuery("Select * from WISHLIST", null);

        if (cursor.getCount() <= 0 || cursor.moveToFirst() == false) {
            Toast.makeText(getApplicationContext(), "Wishlist is empty!", Toast.LENGTH_SHORT).show();
            productByWishlist_support(listObjects);
        } else {
            do {
                String key = cursor.getString(0);
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Products").child(key);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ModelProductList modelProductList = dataSnapshot.getValue(ModelProductList.class);
                        listObjects.add(modelProductList);
                        c[0]++;
                        if (c[0] >= cursor.getCount()) {
                            productByWishlist_support(listObjects);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            } while (cursor.moveToNext());
        }
    }

    public void productByWishlist_support(List<ModelProductList> list) {
        RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), list, cart_item_count, act);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapterProductView);
    }

    public void setFilter() {
        list1 = new ArrayList<>();
        list2 = new ArrayList<>();
        list3 = new ArrayList<>();
        list4 = new ArrayList<>();

        /*Spinner 1 data collection*/
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Category");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list1.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    list1.add(dataSnapshot1.getKey());
                }

                list1.add(0, "Category");
                ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list1);
                arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCat1.setAdapter(arrayAdapter1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*Spinner 2 data collection*/
        spinnerCat1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerCat1.getSelectedItem().equals("Category")) {
                    spinnerCat2.setVisibility(View.GONE);
                    spinnerCat3.setVisibility(View.GONE);
                    spinnerCat4.setVisibility(View.GONE);
                } else {
                    databaseReference.child(spinnerCat1.getSelectedItem().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            list2.clear();
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                list2.add(dataSnapshot1.getKey());
                            }

                            spinnerCat2.setVisibility(View.VISIBLE);
                            list2.add(0, "select");
                            ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list2);
                            arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCat2.setAdapter(arrayAdapter2);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*Spinner 3 data collection*/
        spinnerCat2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerCat2.getSelectedItem().equals("select")) {
                    spinnerCat3.setVisibility(View.GONE);
                } else {
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Category").child(spinnerCat1.getSelectedItem().toString()).child(spinnerCat2.getSelectedItem().toString());
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            list3.clear();
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                list3.add(dataSnapshot1.getKey());
                            }
                            spinnerCat3.setVisibility(View.VISIBLE);
                            list3.add(0, "select");
                            ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list3);
                            arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCat3.setAdapter(arrayAdapter3);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*Spinner 4 data collection*/
        spinnerCat3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerCat3.getSelectedItem().equals("select")) {
                    spinnerCat4.setVisibility(View.GONE);
                } else {
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Category").child(spinnerCat1.getSelectedItem().toString()).child(spinnerCat2.getSelectedItem().toString()).child(spinnerCat3.getSelectedItem().toString());
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String sem = String.valueOf(dataSnapshot.getValue());

                            list4.clear();
                            for (int i = 1; i <= Integer.parseInt(sem); i++) {
                                list4.add("" + i);
                            }
                            spinnerCat4.setVisibility(View.VISIBLE);
                            list4.add(0, "select");
                            ArrayAdapter<String> arrayAdapter4 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list4);
                            arrayAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCat4.setAdapter(arrayAdapter4);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void searchProduct() {
        if (TextUtils.isEmpty(editTextSearchData.getText())) {
            setDataOnPageStart();
            return;
        }

        listObjects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Products");
        databaseReference.orderByChild("f2").startAt(editTextSearchData.getText().toString().toUpperCase()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelProductList modelProductList = dataSnapshot1.getValue(ModelProductList.class);
                    listObjects.add(modelProductList);
                }
                RecyclerAdapterProductView recyclerAdapterProductView = new RecyclerAdapterProductView(getApplicationContext(), listObjects, cart_item_count, act);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BooksListPageNew.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerAdapterProductView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setCartCount() {
        TextView cart_item_count = (TextView) findViewById(R.id.cart_item_count);
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(BooksListPageNew.this.getString(R.string.database_path), null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS P_CART(key VARCHAR, booktype VARCHAR, price VARCHAR, qty VARCHAR);");
        Cursor cursor = sqLiteDatabase.rawQuery("Select * from P_CART ", null);
        cart_item_count.setText("" + cursor.getCount());
    }
}