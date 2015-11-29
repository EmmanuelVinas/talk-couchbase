package evinas.talk.couchbase.shoppinglist.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import evinas.talk.couchbase.shoppinglist.R;
import evinas.talk.couchbase.shoppinglist.eventbus.EventBus;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShowAddItem;

public class ShoppingListFragment extends Fragment{

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.fab_multiple)
    FloatingActionsMenu fabGroup;

    @Bind(R.id.fab_add_action)
    FloatingActionButton fabAdd;

    @Bind(R.id.fab_empty_action)
    FloatingActionButton fabClear;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, null, false);
        setRetainInstance(true);
        ButterKnife.bind(this, view);

        // Init Fab
        fabAdd.setIcon(R.drawable.ic_add_shopping_cart_white_48dp);
        fabClear.setIcon(R.drawable.ic_delete_white_48dp);

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int viewWidth = recyclerView.getMeasuredWidth();
                        float cardViewWidth = 300;
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        layoutManager.setSpanCount(newSpanCount);
                        layoutManager.requestLayout();
                    }
                });
        return view;
    }


    // Shopping Actions
    @OnClick(R.id.fab_add_action)
    public void addItem(){
        fabGroup.collapseImmediately();
        EventBus.post(new ShowAddItem());
    }

    @OnClick(R.id.fab_empty_action)
    public void clearShoppingList(){
        Snackbar.make(recyclerView, "Cart cleared", Snackbar.LENGTH_LONG).show();
    }

}
