package evinas.talk.couchbase.shoppinglist.fragment;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryEnumerator;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import evinas.talk.couchbase.shoppinglist.R;
import evinas.talk.couchbase.shoppinglist.ShoppingListApplication;
import evinas.talk.couchbase.shoppinglist.couchbase.ItemConverter;
import evinas.talk.couchbase.shoppinglist.couchbase.LiveQueryRecyclerAdapter;
import evinas.talk.couchbase.shoppinglist.eventbus.EventBus;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShowAddItem;

public class ShoppingListFragment extends Fragment{

    private final static String SHOPPING_LIST_VIEW_NAME="shoppingList";

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.emptyView)
    View emptyView;

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
                        int newSpanCount =
                                (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ?
                                        4 : 3;
                        layoutManager.setSpanCount(newSpanCount);
                        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
                        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacingInPixels, true));
                        layoutManager.requestLayout();
                    }
                });

        // Couchbase
        com.couchbase.lite.View shoppingView = getShoppingView();
        final LiveQueryRecyclerAdapter adapter = new LiveQueryRecyclerAdapter(getContext(), shoppingView.createQuery().toLiveQuery());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }


    private com.couchbase.lite.View getShoppingView() {
        com.couchbase.lite.View view = ((ShoppingListApplication)getActivity().getApplication()).getDatabase().getView(SHOPPING_LIST_VIEW_NAME);
        if (view.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get(ItemConverter.PROPERTY_TYPE);
                    if (ItemConverter.PROPERTY_TYPE_VALUE.equals(type)) {
                        emitter.emit((String) document.get(ItemConverter.PROPERTY_ID), document);
                    }
                }
            };
            view.setMap(mapper, "1");
        }
        return view;
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spacing, boolean includeEdge) {
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int spanCount = ((GridLayoutManager)parent.getLayoutManager()).getSpanCount();
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }


    // Shopping Actions
    @OnClick(R.id.fab_add_action)
    public void addItem(){
        Log.i(ShoppingListApplication.TAG, "Event:addItem");
        fabGroup.collapseImmediately();
        EventBus.post(new ShowAddItem());
    }

    @OnClick(R.id.fab_empty_action)
    public void clearShoppingList(){
        Log.i(ShoppingListApplication.TAG, "Event:clearShoppingList");
        try {
            QueryEnumerator enumerator = getShoppingView().createQuery().run();
            ((ShoppingListApplication)getActivity().getApplication()).getDatabase().beginTransaction();
            while (enumerator.hasNext()){
                Document document = enumerator.next().getDocument();
                document.delete();
                document.purge();
            };
            ((ShoppingListApplication)getActivity().getApplication()).getDatabase().endTransaction(true);
        } catch (CouchbaseLiteException e) {
            Log.e(ShoppingListApplication.TAG, "Unable to remove documents");
        }
    }

}
