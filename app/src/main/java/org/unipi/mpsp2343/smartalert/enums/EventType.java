package org.unipi.mpsp2343.smartalert.enums;

import androidx.annotation.IntDef;

//Valid event types
@IntDef({EventType.FLOOD, EventType.FIRE, EventType.EARTHQUAKE, EventType.TORNADO})
public @interface EventType {
    int FLOOD = 0;
    int FIRE = 1;
    int EARTHQUAKE = 2;
    int TORNADO = 3;
}
