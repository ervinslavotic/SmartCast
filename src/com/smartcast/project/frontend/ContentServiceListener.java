package com.smartcast.project.frontend;

/**
 * Created by Slavotic on 02/06/14.
 */
import com.smartcast.project.services.ContentService;

public interface ContentServiceListener {
    public void onContentServiceChanged(ContentService service);
}
