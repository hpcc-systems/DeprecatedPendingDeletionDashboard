package org.hpccsystems.dashboard.service.hipie;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.HpccConnection;

public interface CompositionService {

	Composition createComposition(String userId, String compName,HpccConnection hpccConnection)throws Exception;

	Composition updateComposition(String userId, String compName);

	CompositionInstance runComposition(String userId, String compName);

	boolean deleteComposition(String userId, String compName);

}
