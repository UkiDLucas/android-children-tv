package com.cyberwalkabout.common.util;

import android.os.Bundle;

public interface ActivityLifecycleCallbacks<T>
{
	void onActivityCreated(T activity, Bundle savedInstanceState);

	void onActivityStarted(T activity);

	void onActivityResumed(T activity);

	void onActivityPaused(T activity);

	void onActivityStopped(T activity);

	void onActivitySaveInstanceState(T activity, Bundle outState);

	void onActivityDestroyed(T activity);
}