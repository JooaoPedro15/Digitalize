package app;

import static spark.Spark.*;
import service.ApiConfig;
import service.Routes;

public class Main
{
    public static void main(String[] args)
    {
        port(getPort());
        ApiConfig.enableCors();
        Routes.mount();
    }

    private static int getPort()
    {
        String p = System.getenv("PORT");
        if (p == null || p.isBlank())
        {
            return 4567;
        }
        return Integer.parseInt(p);
    }
}
