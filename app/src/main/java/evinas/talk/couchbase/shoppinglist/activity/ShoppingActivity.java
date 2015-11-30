package evinas.talk.couchbase.shoppinglist.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    private AddItemFragment addItemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        database = ((ShoppingListApplication)getApplication()).getDatabase();

        ButterKnife.bind(this);

        // Set Action Bar
        setSupportActionBar(toolbar);

        if (savedInstanceState == null){
            shoppingListFragment = new ShoppingListFragment();
            replaceFragment(R.id.layout_content, shoppingListFragment, false);
        }
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


    @Subscribe
    public void showAddItemFragment(ShowAddItem addItem){
        // Display add item fragment
        addItemFragment = new AddItemFragment();
        replaceFragment(R.id.layout_content, addItemFragment, false);
    }

    @Subscribe
    public void addItemToShoppingList(ShoppingItem shoppingItem){
        replaceFragment(R.id.layout_content, shoppingListFragment, false);
        Document document = database.createDocument();
        ItemConverter.fillDocument(document, shoppingItem);
    }


    protected void replaceFragment(int containerId, Fragment fragment, boolean addToBackStack) {
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
