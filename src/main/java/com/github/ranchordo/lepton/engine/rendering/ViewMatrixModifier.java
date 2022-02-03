package com.github.ranchordo.lepton.engine.rendering;

import com.bulletphysics.linearmath.Transform;

public interface ViewMatrixModifier {
	public Transform modifyViewMatrix(Transform view);
}
