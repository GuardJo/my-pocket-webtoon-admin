package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import java.util.List;

public abstract class AbstractPageableControllerTest {
    protected record MockPageResponse<T>(
            List<T> content,
            AbstractPageableControllerTest.MockPageMetadata page
    ) {
    }

    protected record MockPageMetadata(
            long totalElements
    ) {
    }
}
