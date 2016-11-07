package com.greentopli.core;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.greentopli.core.handler.CartDbHandler;
import com.greentopli.core.remote.ServiceGenerator;
import com.greentopli.core.remote.UserService;
import com.greentopli.model.EntityList;
import com.greentopli.model.PurchasedItem;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rnztx on 24/10/16.
 */

public class OrderHistoryService extends IntentService {
	private static final String TAG = OrderHistoryService.class.getSimpleName();
	public static final String ACTION_PROCESSING = "SERVICE_PROCESSING";
	public static final String ACTION_PROCESSING_COMPLETE = "SERVICE_PROCESSING_COMPLETE";
	public static final String ACTION_PROCESSING_FAILED = "SERVICE_PROCESSING_FAILED";

	public OrderHistoryService(){
		super(OrderHistoryService.class.getSimpleName());
	}

	CartDbHandler cartDbHandler ;
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e(TAG,intent.getDataString());
		String user_id = intent.getDataString();

		if (user_id!=null && !user_id.isEmpty()){
			broadcast(ACTION_PROCESSING);
			UserService service = ServiceGenerator.createService(UserService.class);
			Call<EntityList<PurchasedItem>> call = service.getUserOrderHistory(user_id);
			cartDbHandler = new CartDbHandler(getApplicationContext());

			call.enqueue(new Callback<EntityList<PurchasedItem>>() {
				@Override
				public void onResponse(Call<EntityList<PurchasedItem>> call, Response<EntityList<PurchasedItem>> response) {
					if (response.body()!=null && response.body().getItems()!=null && !response.body().getItems().isEmpty()){
							cartDbHandler.storeOrderHistory(
									response.body().getItems()
							);
					}
					// empty case will be handled by presenter
					broadcast(ACTION_PROCESSING_COMPLETE);
				}

				@Override
				public void onFailure(Call<EntityList<PurchasedItem>> call, Throwable t) {
					broadcast(ACTION_PROCESSING_FAILED);
					t.printStackTrace();
				}
			});
		}
	}

	private void broadcast(String action){
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(action);
		sendBroadcast(broadcastIntent);
	}
}