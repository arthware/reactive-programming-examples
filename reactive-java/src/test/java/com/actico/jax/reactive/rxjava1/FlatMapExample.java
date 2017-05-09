package com.actico.jax.reactive.rxjava1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Single;

public class FlatMapExample
{
   static final Logger LOG = LoggerFactory.getLogger(FlatMapExample.class);


   @Test
   public void flatMapMewsAggregator() throws Exception
   {

      Observable<String> urls = Observable
         .just("http://www.spiegel.de/schlagzeilen/tops/index.rss",
            "https://www.heise.de/newsticker/heise.rdf");

      Observable<Element> newsItemStream = urls
         .flatMap(singleUrl -> {
            LOG.info("f(url): Observable<Element>: {}", singleUrl);
            Single<Document> document = Single.fromCallable(() -> Jsoup.connect(singleUrl).get());
            return document.retry(2).toObservable()
               .flatMap(doc -> Observable.from(doc.select("item")));
         });


      Observable<NewsTitle> news = newsItemStream

         .map(newsItem -> {
            NewsTitle newsTitle =
               new NewsTitle(newsItem.select("item title").text(),
                  newsItem.select("item link").text());
            return newsTitle;
         });

      news.toBlocking().forEach(doc -> System.out.println(doc));


      /*  points.flatMap(flatMapPoints())
         .subscribe(p -> System.out.println("" + p + " " + p.getClass().getSimpleName()));*/
   }

   static class NewsTitle
   {
      String title;
      String source = "";
      String url;

      public NewsTitle(String title, String url)
      {
         super();
         this.title = title;
         this.source = source;
         this.url = url;
      }

      @Override
      public String toString()
      {
         return "NewsTitle [title=" + title + ", source=" + source + ", url=" + url + "]";
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((title == null) ? 0 : title.hashCode());
         result = prime * result + ((url == null) ? 0 : url.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         NewsTitle other = (NewsTitle) obj;
         if (title == null)
         {
            if (other.title != null)
               return false;
         }
         else if (!title.equals(other.title))
            return false;
         if (url == null)
         {
            if (other.url != null)
               return false;
         }
         else if (!url.equals(other.url))
            return false;
         return true;
      }


   }

}
