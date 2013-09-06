/**
 * 
 */
package com.softsec.tase.node.util.domain;

import com.softsec.tase.common.rpc.domain.container.BundleType;
import com.softsec.tase.common.rpc.domain.container.Context;
import com.softsec.tase.node.exception.PreparationException;

/**
 * ProgramHandler
 * <p> </p>
 * @author yanwei
 * @since 2013-9-4 下午5:33:29
 * @version
 */
public class ProgramHandler {

	public static void load(Context context) throws PreparationException {
		if (context.getBundleType().equals(BundleType.ZIP)) {
			
		}
	}
}
