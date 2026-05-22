package com.heremobby.heremobby.framework;

import org.bukkit.entity.Entity;
import java.util.UUID;

public interface CustomEntity {
    UUID getUniqueId();
    Entity getBaseEntity();
    void remove();
    String getTypeId();
}
