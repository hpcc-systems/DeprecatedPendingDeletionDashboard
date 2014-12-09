package org.hpccsystems.dashboard.service.hipie;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.chart.entity.HPCCConnection;
import org.hpccsystems.dashboard.entity.Widget;

public interface CompositionService {

	Composition createComposition(String compName,
			HPCCConnection hpccConnection, Widget widget) throws Exception;

	Composition updateComposition(String userId, String compName);

	CompositionInstance runComposition(String userId, String compName);

	boolean deleteComposition(String userId, String compName);

}
