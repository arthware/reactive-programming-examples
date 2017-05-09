package com.actico.jax.reactive.rxjava2;

import org.springframework.web.client.RestTemplate;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;

public class RxJava2CustomObservable
{
   /***
    * Shows how to create a custom observable without backpressure support. 
    * <p>
    *    WARNING: This way is NOT recommended by RxJava. For demonstration purposes only.
    * </p>
    * 
    * @param url the url to POST to.
    * @param postPayload the object to POST.
    * @return observable using Springs rest template
    */
   static <T> Observable<T> httpPostObservable(String url, Object postPayload, Class<T> resultType)
   {
      return Observable.create(obs -> {
         RestTemplate http = new RestTemplate();
         try
         {
            T result = http.postForObject(url, postPayload, resultType);
            if (!obs.isDisposed())
            {
               obs.onNext(result);
               obs.onComplete();
            }
         }
         catch (Throwable e)
         {
            obs.onError(e);
         }
      });
   }


   /**
    * Naive implementation of Single.create() Wrapping Springs RestTemplate
    *
    * @return created single
    */
   static <T> Single<T> httpPostSingle(String url, Object postPayload, Class<T> resultType)
   {
      return Single.create(obs -> {
         RestTemplate http = new RestTemplate();
         try
         {
            T result = http.postForObject(url, postPayload, resultType);
            if (!obs.isDisposed())
            {
               obs.onSuccess(result);
            }
         }
         catch (Throwable e)
         {
            obs.onError(e);
         }
      });
   }


   /**
    * Same as {@link #httpPostObservable(String, Object, Class)} but without lambdas.
    * 
    * @param url the url to POST to.
    * @param postPayload the object to POST.
    * @return observable using Springs rest template
    */
   static <T> Observable<T> createCustomObservableNonLambda(String url, Object postPayload, Class<T> resultType)
   {
      return Observable.create(new ObservableOnSubscribe<T>()
      {
         @Override
         public void subscribe(ObservableEmitter<T> obs) throws Exception
         {
            RestTemplate http = new RestTemplate();
            try
            {
               T result = http.postForObject(url, postPayload, resultType);
               if (!obs.isDisposed())
               {
                  obs.onNext(result);
                  obs.onComplete();
               }
            }
            catch (Throwable e)
            {
               obs.onError(e);
            }
         }
      });
   }


}
