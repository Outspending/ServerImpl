package me.outspending.entity;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import me.outspending.Tickable;
import me.outspending.entity.meta.EntityMeta;
import me.outspending.player.Player;
import me.outspending.position.Pos;
import me.outspending.protocol.packets.client.play.ClientRemoveEntitiesPacket;
import me.outspending.protocol.packets.client.play.ClientSetEntityMetaPacket;
import me.outspending.protocol.packets.client.play.ClientSpawnEntityPacket;
import me.outspending.protocol.packets.client.play.ClientTeleportEntityPacket;
import me.outspending.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class Entity implements Viewable, Tickable {
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);

    private final List<Entity> viewers = new ArrayList<>();
    protected final EntityType type;
    protected final int entityID;
    protected final UUID uuid;
    protected EntityMeta entityMeta = new EntityMeta();

    protected boolean onGround = true;
    protected Pos position = Pos.ZERO;
    protected World world;

    public Entity(@NotNull EntityType type) {
        this(type, UUID.randomUUID());
    }

    public Entity(@NotNull EntityType type, @NotNull UUID uuid) {
        this.type = type;
        this.entityID = EntityCounter.getNextEntityID();
        this.uuid = uuid;
    }

    public static @NotNull Builder builder(@NotNull EntityType type) {
        return new Builder(type);
    }

    public void setRotation(float yaw, float pitch) {
        setPosition(new Pos(position.x(), position.y(), position.z(), yaw, pitch));
    }

    public void setMeta(@NotNull EntityMeta meta) {
        this.entityMeta = meta;
        sendPacketsToViewers(new ClientSetEntityMetaPacket(this));
    }

    public void spawn(@NotNull Player player) {
        Preconditions.checkNotNull(this.world, "Spawning entity without a world!");

        player.sendPacket(new ClientSpawnEntityPacket(this));
//        player.sendBundledPackets(
//                new ClientSpawnEntityPacket(this),
//                new ClientSetEntityMetaPacket(this)
//        );
    }

    public void spawn(@NotNull Player... players) {
        this.spawn(List.of(players));
    }

    public void spawn(@NotNull Collection<Player> players) {
        players.forEach(this::spawn);
    }

    public void spawnGlobal() {
        world.addEntity(this);
        this.spawn(world.getPlayers());
    }

    public void teleport(@NotNull Pos pos) {
        this.setPosition(pos);
        sendPacketsToViewers(new ClientTeleportEntityPacket(this, pos));
    }

    @Contract("null -> fail")
    public double distanceFrom(@UnknownNullability Entity entity) {
        return distanceFrom(entity.getPosition());
    }

    @Contract("null -> fail")
    public double distanceFrom(@UnknownNullability Pos position) {
        Preconditions.checkNotNull(position, "Position cannot be null");

        return getPosition().distance(position);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entity entity && this.uuid == entity.uuid;
    }

    @Override
    public void addViewer(@NotNull Entity entity) {
        viewers.add(entity);
    }

    @Override
    public void removeViewer(@NotNull Entity entity) {
        viewers.remove(entity);
    }

    @Override
    public @NotNull List<Entity> getViewers() {
        return viewers;
    }

    @Override
    public void updateViewers() {
        if (world == null) return;

        for (Entity entity : world.getAllEntities()) {
            if (this.equals(entity)) {
                continue;
            }

            boolean isViewer = this.isViewer(entity);
            double distance = this.distanceFrom(entity);

            int viewableDistance = this.entityMeta.getViewableDistance();
            if (distance <= viewableDistance && !isViewer) {
                logger.info("Adding Viewer: {}", entity.entityID);
                this.addViewer(entity);
                if (entity instanceof Player player) {
                    player.sendPacket(new ClientSpawnEntityPacket(this));
                }
            } else if (distance >= viewableDistance && isViewer) {
                logger.info("Removing Viewer: {}", entity.entityID);
                this.removeViewer(entity);
                if (entity instanceof Player player) {
                    player.sendRemoveEntitiesPacket(this);
                }
            }
        }
    }

    @Override
    public void tick(long time) {
        updateViewers();
    }

    public enum Pose {
        STANDING,
        FALL_FLYING,
        SLEEPING,
        SWIMMING,
        SPIN_ATTACK,
        SNEAKING,
        LONG_JUMPING,
        DYING,
        CROAKING,
        USING_TONGUE,
        SITTING,
        ROARING,
        SNIFFING,
        EMERGING,
        DIGGING;

        public static @NotNull Pose getById(@Range(from = 0, to = 14) int id) {
            final Pose value = values()[id];
            return value != null ? value : STANDING;
        }

    }

    public static class Builder {
        private final EntityType type;
        private UUID entityUUID = UUID.randomUUID();
        private Pos position = Pos.ZERO;
        private World world;

        public Builder(EntityType type) {
            this.type = type;
        }

        @Contract("_ -> this")
        public @NotNull Builder setEntityUUID(@NotNull UUID entityUUID) {
            this.entityUUID = entityUUID;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder setPosition(@NotNull Pos position) {
            this.position = position;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder setWorld(@NotNull World world) {
            this.world = world;
            return this;
        }

        public @NotNull Entity build() {
            Preconditions.checkNotNull(world, "World cannot be null");

            final Entity entity = new Entity(type, entityUUID);
            entity.setPosition(this.position);
            entity.setWorld(this.world);

            return entity;
        }

    }

}
