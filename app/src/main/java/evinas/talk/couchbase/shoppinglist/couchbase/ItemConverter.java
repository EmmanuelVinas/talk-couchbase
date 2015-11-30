package evinas.talk.couchbase.shoppinglist.couchbase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import evinas.talk.couchbase.shoppinglist.ShoppingListApplication;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShoppingItem;

public final class ItemConverter {

    public final static String PROPERTY_TYPE = "type";
    public final static String PROPERTY_TYPE_VALUE = "item";
    public final static String PROPERTY_TITLE = "title";
    public final static String PROPERTY_DESCRIPTION = "description";
    public final static String PROPERTY_COUNT = "count";
    public static final String ATTACHED_IMAGE ="attached_img";
    public final static String PROPERTY_ID = "_id";

    public static void fillDocument(Document document, ShoppingItem obj) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PROPERTY_TYPE, PROPERTY_TYPE_VALUE);
        properties.put(PROPERTY_TITLE, obj.getTitle());
        properties.put(PROPERTY_DESCRIPTION, obj.getDescription());
        properties.put(PROPERTY_COUNT, obj.getNumber());
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            Log.e(ShoppingListApplication.TAG, "Error while saving properties");
        }
        attachImage(document, obj.getImage());
    }

    public static ShoppingItem fromDocument(Document document) {
        Map<String, Object> properties = document.getProperties();
        ShoppingItem item = new ShoppingItem();
        if (properties != null){
            item.setTitle((String) properties.get(PROPERTY_TITLE));
            item.setDescription((String) properties.get(PROPERTY_DESCRIPTION));
            Integer count = (Integer) properties.get(PROPERTY_COUNT);
            if (count != null){
                item.setNumber((Integer) properties.get(PROPERTY_COUNT));
            }
        }
        item.setImage(loadImage(document));
        return item;
    }

    public static void attachImage(Document doc, Bitmap bitmap) {
        if (doc == null || bitmap == null) return;
        InputStream stream = bitmapToStream(bitmap);
        if (stream != null){
            UnsavedRevision revision = doc.createRevision();
            revision.setAttachment(ATTACHED_IMAGE, "image/png", stream);
            try {
                revision.save();
            } catch (CouchbaseLiteException e) {
                Log.e(ShoppingListApplication.TAG, "Error while saving an image");
            }
        }
    }

    public static InputStream bitmapToStream(Bitmap image){
        if (image != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
            return new ByteArrayInputStream(out.toByteArray());
        }
        return null;
    }

    public static URL getImageUrl(Document doc){
        if (doc == null) return null;
        Attachment attachment = doc.getCurrentRevision().getAttachment(ATTACHED_IMAGE);
        return attachment != null ? attachment.getContentURL(): null;
    }

    // Don't do that in real world. Use Asynchronous loading.
    public static Bitmap loadImage(Document doc){
        URL url = getImageUrl(doc);
        if (url != null){
            InputStream is = null;
            try {
                is = url.openStream();
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                Log.e(ShoppingListApplication.TAG, "Unable to load url :" + url);
                return null;
            }
        }
        return null;
    }


}
