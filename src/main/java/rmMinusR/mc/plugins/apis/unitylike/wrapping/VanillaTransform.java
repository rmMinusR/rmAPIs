package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.data.Matrix;
import rmMinusR.mc.plugins.apis.unitylike.data.Quaternion;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class VanillaTransform extends Component implements Transform {

    private Entity backingEntity;

    public VanillaTransform(Entity vanilla) {
        super(null);
        backingEntity = vanilla;
    }

    public VanillaTransform(WrappedEntity gameObject) {
        super(gameObject);
        backingEntity = gameObject.entity;
    }

    @Override
    public Matrix WorldToLocal() {
        return LocalToWorld().Inverse();
    }

    @Override
    public Matrix LocalToWorld() {
        return Matrix.Translate(GetPosition()) * GetRotation().ToMatrix();
    }

    @Override
    public Vector3 GetPosition() {
        return new Vector3( backingEntity.getLocation() );
    }

    @Override
    public void SetPosition(Vector3 position) { // FIXME kludgey
        Location tmp = backingEntity.getLocation();
        tmp.setX(position.x);
        tmp.setY(position.y);
        tmp.setZ(position.z);
        backingEntity.teleport(tmp, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public Quaternion GetRotation() {
        Location tmp = backingEntity.getLocation();
        return Quaternion.FromEulerAngles(new Vector3(tmp.getYaw(), tmp.getPitch(), 0));
    }

    @Override
    public void SetRotation(Quaternion rotation) {
        Vector3 euler = rotation.ToEulerAngles();
        Location tmp = backingEntity.getLocation();
        tmp.setPitch(euler.x);
        tmp.setYaw(euler.y);
        backingEntity.teleport(tmp, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public Vector3 GetScale() {
        return Vector3.one();
    }

    @Override
    public void SetScale(Vector3 scale) {
        throw new IllegalStateException("Vanilla entities cannot be rescaled");
    }

    @Override
    public Vector3 Right() { // FIXME kludge
        Matrix tmp = GetRotation().ToMatrix();
        return new Vector3(tmp.m[0][0], tmp.m[0][1], tmp.m[0][2]);
    }

    @Override
    public Vector3 Up() { // FIXME kludge
        Matrix tmp = GetRotation().ToMatrix();
        return new Vector3(tmp.m[1][0], tmp.m[1][1], tmp.m[1][2]);
    }

    @Override
    public Vector3 Forward() {
        return new Vector3( backingEntity.getLocation().getDirection() );
    }

    @Override
    public void CopyDataFrom(Transform t) {
        SetPosition(t.GetPosition());
        SetRotation(t.GetRotation());
    }

    @Override
    public VanillaTransform clone() {
        return new VanillaTransform(backingEntity);
    }
}
