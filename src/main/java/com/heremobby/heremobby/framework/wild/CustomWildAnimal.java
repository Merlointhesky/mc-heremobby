package com.heremobby.heremobby.framework.wild;

import com.heremobby.heremobby.framework.CustomEntity;
import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import java.util.UUID;

public class CustomWildAnimal implements CustomEntity {
    private final String typeId;
    private final String modelId;
    private final EntityType baseType;
    private Entity baseEntity;

    public CustomWildAnimal(String typeId, String modelId, EntityType baseType) {
        this.typeId = typeId;
        this.modelId = modelId;
        this.baseType = baseType;
    }

    public void spawn(Location loc) {
        this.baseEntity = EntityBuilder.createBaseEntity(loc, baseType, typeId);
        EntityBuilder.applyBetterModel(baseEntity, modelId);
    }

    @Override
    public UUID getUniqueId() {
        return baseEntity != null ? baseEntity.getUniqueId() : null;
    }

    @Override
    public Entity getBaseEntity() {
        return baseEntity;
    }

    @Override
    public void remove() {
        if (baseEntity != null) baseEntity.remove();
    }

    @Override
    public String getTypeId() {
        return typeId;
    }
}
