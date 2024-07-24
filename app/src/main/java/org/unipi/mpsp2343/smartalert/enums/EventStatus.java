package org.unipi.mpsp2343.smartalert.enums;

import androidx.annotation.IntDef;

//Valid event statuses
@IntDef({EventStatus.OPEN, EventStatus.REJECTED, EventStatus.CONFIRMED})
public @interface EventStatus {
    int OPEN = 0;
    int REJECTED = 1;
    int CONFIRMED = 2;
}
