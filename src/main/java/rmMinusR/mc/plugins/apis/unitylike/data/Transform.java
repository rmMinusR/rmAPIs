package rmMinusR.mc.plugins.apis.unitylike.data;

public interface Transform extends Cloneable {

    Matrix WorldToLocal();
    Matrix LocalToWorld();

    Vector3 GetPosition();
    void SetPosition(Vector3 position);

    Quaternion GetRotation();
    void SetRotation(Quaternion rotation);

    Vector3 GetScale();
    void SetScale(Vector3 scale);

    Vector3 Right();
    Vector3 Up();
    Vector3 Forward();

    void CopyDataFrom(Transform t);
    Transform clone();
}
