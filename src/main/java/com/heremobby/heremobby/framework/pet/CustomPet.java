package com.heremobby.heremobby.framework.pet;

import com.heremobby.heremobby.HereMobbyPlugin;
import com.heremobby.heremobby.framework.CustomEntity;
import com.heremobby.heremobby.framework.EntityBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;

public class CustomPet implements CustomEntity {
    private final String typeId;
    private final String modelId;
    private final UUID ownerId;
    private Entity baseEntity;
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(HereMobbyPlugin.getInstance(), "pet_owner");

    public CustomPet(String typeId, String modelId, UUID ownerId) {
        this.typeId = typeId;
        this.modelId = modelId;
        this.ownerId = ownerId;
    }

    public void spawn(Location loc) {
        // Using Wolf as a base engine for pets
        this.baseEntity = EntityBuilder.createBaseEntity(loc, EntityType.WOLF, typeId);
        this.baseEntity.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, ownerId.toString());
        
        EntityBuilder.applyBetterModel(baseEntity, modelId);
        
        if (baseEntity instanceof Mob mob) {
            Bukkit.getMobGoals().removeAllGoals(mob);
            Bukkit.getMobGoals().addGoal(mob, 1, new FollowOwnerGoal(HereMobbyPlugin.getInstance(), mob, ownerId));
        }
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
