package ratpack.fibonacci;

import java.net.URI;
import java.time.Duration;

import ratpack.exec.Promise;
import ratpack.http.client.HttpClient;
import ratpack.server.RatpackServer;
import ratpack.http.client.ReceivedResponse;

public class Fibonacci {

	public static void main(String[] args) throws Exception {
		
		final HttpClient httpClient = HttpClient.of(httpSpec -> httpSpec
				.readTimeout(Duration.ofMinutes(2)));

		RatpackServer.start(server -> server
				 .serverConfig(
						 serverConfigBuilder -> serverConfigBuilder
						 	.development(Boolean.FALSE)
						 	.port(5050)
						 	.threads(1))
			     .handlers(chain -> chain
			       .get(ctx -> ctx.render("Hello World!"))
			       .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!"))
			       .prefix("fib", fib -> fib.get(":n", ctx -> {
			    	   final Long n = Long.parseLong(ctx.getPathTokens().get("n"));
			    	   if(n <= 2) {
			    		   ctx.render("1");
			    	   } else {
			    		   final Promise<ReceivedResponse> httpPromiseFib1 = 
			    				   httpClient.get(new URI("http://localhost:5050/fib/" + (n - 1)));
			    		   
			    		   final Promise<Long> promiseFib1 = httpPromiseFib1.map(response -> 
			    		   						Long.parseLong(response.getBody().getText()));
			    		   
			    		   final Promise<ReceivedResponse> httpPromiseFib2 = 
			    				   httpClient.get(new URI("http://localhost:5050/fib/" + (n - 2)));
			    		   
			    		   final Promise<Long> promiseFib2 = httpPromiseFib2.map(response -> 
			    		   						Long.parseLong(response.getBody().getText()));
			    		   
			    		   promiseFib1.then(fib1 -> 
			    		   		promiseFib2.then(fib2 -> 
			    		   			ctx.render(String.valueOf(fib1 + fib2))));
			    	   }
			       }))
			     )
			   );
	}

}
