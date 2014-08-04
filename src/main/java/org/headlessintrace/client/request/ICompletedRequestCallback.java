package org.headlessintrace.client.request;

import java.util.List;


import org.headlessintrace.client.model.ITraceEvent;


public interface ICompletedRequestCallback {
		/**
		 * The contents of this method must be thread safe.
		 * @param t
		 * @throws WuqispankException 
		 */
		void requestCompleted(IRequest events);
}
