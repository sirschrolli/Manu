package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

/** Meerjungfrauen und Tritonwachen (seit 0.93): ein Modell, Zubehör je Beruf, Textur je Gestalt. */
public interface MerfolkLike {
    MermaidProfession profession();

    MermaidLook look();

    /** Nur auf dem Client: wie weit die Gestalt gerade schwimmt (0 = wassertretend aufrecht, 1 = waagrecht). */
    float swimPose(float partialTicks);

    /** Nur auf dem Client: Phase des Schwanzschlags. */
    float swimPhase(float partialTicks);
}
