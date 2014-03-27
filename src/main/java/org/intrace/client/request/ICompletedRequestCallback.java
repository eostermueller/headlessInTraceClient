package org.intrace.client.request;

import java.util.List;

import org.intrace.client.model.ITraceEvent;


public interface ICompletedRequestCallback {
		/**
		 * The contents of this method must be thread safe.
		 * @param t
		 */
		void requestCompleted(IRequest events);
}
