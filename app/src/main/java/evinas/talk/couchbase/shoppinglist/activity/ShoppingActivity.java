package evinas.talk.couchbase.shoppinglist.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import evinas.talk.couchbase.shoppinglist.R;
import evinas.talk.couchbase.shoppinglist.ShoppingListApplication;
import evinas.talk.couchbase.shoppinglist.couchbase.ItemConverter;
import evinas.talk.couchbase.shoppinglist.eventbus.EventBus;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShoppingItem;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShowAddItem;
import evinas.talk.couchbase.shoppinglist.fragment.AddItemFragment;
import evinas.talk.couchbase.shoppinglist.fragment.ShoppingListFragment;

public class ShoppingActivity extends AppCompatActivity {

    @Bind(R.id.coordinatorLayout)
    ViewGroup topLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Database database;

    private ShoppingListFragment shoppingListFragment;

    private Status currentState = null;

    private enum Status{LIST, ADD};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        database = ((ShoppingListApplication)getApplication()).getDatabase();

        ButterKnife.bind(this);

        // Set Action Bar
        setSupportActionBar(toolbar);

        if (savedInstanceState == null){
            showShoppingListFragment();
        }
   }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String state = savedInstanceState.getString("Status");
        currentState = state != null ? Status.valueOf(state) : Status.ADD;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString("Status", currentState.toString());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (currentState == Status.ADD){
            showShoppingListFragment();
        }else{
            super.onBackPressed();
        }

    }

    @Subscribe
    public void showAddItemFragment(ShowAddItem addItem){
        Log.i(ShoppingListApplication.TAG, "Event:showAddItemFragment");
        if (currentState != Status.ADD){
            // Display add item fragment
            replaceFragment(R.id.layout_content, new AddItemFragment(), false);
            currentState=Status.ADD;
        }
    }

    public void showShoppingListFragment(){
        if (currentState != Status.LIST){
            shoppingListFragment = shoppingListFragment != null ? shoppingListFragment : new ShoppingListFragment();
            replaceFragment(R.id.layout_content, shoppingListFragment, false);
            currentState=Status.LIST;
        }
    }

    @Subscribe
    public void addItemToShoppingList(ShoppingItem shoppingItem){
        Log.i(ShoppingListApplication.TAG, "Event:addItemToShoppingList");
        Document document = database.createDocument();
        ItemConverter.fillDocument(document, shoppingItem);
        showShoppingListFragment();
    }


    private void replaceFragment(int containerId, Fragment fragment, boolean addToBackStack) {
        if (fragment != null){
            String backStateName = fragment.getClass().getName();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(containerId, fragment, backStateName);
            if (addToBackStack) {
                ft.addToBackStack(backStateName);
            }
            ft.commit();
        }
    }
}
