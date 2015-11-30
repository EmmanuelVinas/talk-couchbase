package evinas.talk.couchbase.shoppinglist.couchbase;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;

import butterknife.Bind;
import butterknife.ButterKnife;
import evinas.talk.couchbase.shoppinglist.R;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShoppingItem;

public class LiveQueryRecyclerAdapter extends RecyclerView.Adapter<LiveQueryRecyclerAdapter.LiveQueryViewHolder> {

    public Context context;
    private QueryEnumerator enumerator;
    private Handler mainHandler;


    public LiveQueryRecyclerAdapter(Context context, LiveQuery query) {
        this.context = context;
        this.mainHandler = new Handler(context.getMainLooper());

        query.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(final LiveQuery.ChangeEvent event) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        enumerator = event.getRows();
                        notifyDataSetChanged();
                    }
                });
            }
        });
        query.start();
    }

    @Override
    public LiveQueryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.item, parent, false);
        return new LiveQueryViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(LiveQueryViewHolder holder, int position) {
        ShoppingItem item = getItem(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewDescription.setText(item.getDescription());
        holder.textViewCount.setText(""+item.getNumber());
        holder.imageView.setImageBitmap(item.getImage());
    }

    public static class LiveQueryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.title)
        TextView textViewTitle;

        @Bind(R.id.description)
        TextView textViewDescription;

        @Bind(R.id.count)
        TextView textViewCount;

        @Bind(R.id.image)
        ImageView imageView;

        public LiveQueryViewHolder(View itemView, int viewType) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public int getItemCount() {
        return enumerator != null ? enumerator.getCount() : 0;
    }

    public ShoppingItem getItem(int i) {
        return enumerator != null ? ItemConverter.fromDocument(enumerator.getRow(i).getDocument()) : null;
    }
}
