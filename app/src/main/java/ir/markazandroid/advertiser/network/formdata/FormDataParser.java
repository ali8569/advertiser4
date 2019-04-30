package ir.markazandroid.advertiser.network.formdata;

import android.net.Uri;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Coded by Ali on 29/11/2017.
 */

public class FormDataParser {

    public static FormBody objectToFormBody(Object object) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder(Charset.forName("UTF-8"));
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Form.class)) {
                field.setAccessible(true);
                String name;
                if (!field.getAnnotation(Form.class).name().equals(""))
                    name = field.getAnnotation(Form.class).name();
                else {
                    name = field.getName();
                }
                try {
                    Object o = field.get(object);
                    if (o != null)
                        formBodyBuilder.add(name, o.toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return formBodyBuilder.build();
    }


    public static MultipartBody objectToMultipartBody(Object object) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Form.class)) {
                field.setAccessible(true);
                String name;
                if (!field.getAnnotation(Form.class).name().equals(""))
                    name = field.getAnnotation(Form.class).name();
                else {
                    name = field.getName();
                }
                try {
                    Object o = field.get(object);
                    if (o != null) {
                        if (field.getAnnotation(Form.class).type().equalsIgnoreCase(Form.FILE)) {
                            if (!o.toString().startsWith("http")) {
                                File file = new File(Uri.parse(o.toString()).getPath());
                                multipartBodyBuilder.addFormDataPart(name, name,
                                        RequestBody.create(MultipartBody.FORM, file));
                            }
                        } else if (field.getAnnotation(Form.class).type().equalsIgnoreCase(Form.MAP)) {
                            Map map = (Map) o;
                            for (Object entry : map.entrySet()) {
                                multipartBodyBuilder.addFormDataPart(name + "['" + ((Map.Entry) entry).getKey() + "']",
                                        ((Map.Entry) entry).getValue().toString());
                            }

                        } else if (field.getAnnotation(Form.class).type().equalsIgnoreCase(Form.BYTE)) {
                            boolean value = (boolean) o;
                            multipartBodyBuilder.addFormDataPart(name, (value ? 1 : 0) + "");
                        } else if (field.getAnnotation(Form.class).type().equalsIgnoreCase(Form.COLLECTION)) {
                            Collection collection = (Collection) o;
                            Iterator iterator = collection.iterator();
                            for (int i=0;i<collection.size();i++) {
                                multipartBodyBuilder.addFormDataPart(name + "['" + i + "']",
                                        iterator.next().toString());
                            }
                        } else {
                            multipartBodyBuilder.addFormDataPart(name, o.toString());
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return multipartBodyBuilder.build();
    }
}
