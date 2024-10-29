package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@WebServlet("/poke")
public class PokeServlet extends HttpServlet {

    private String getPokeData(String identifier) throws IOException {
        String urlString;
        if (isNumeric(identifier)) {
            urlString = "https://pokeapi.co/api/v2/pokemon/" + identifier; // Buscar por ID
        } else {
            urlString = "https://pokeapi.co/api/v2/pokemon/" + identifier.toLowerCase(); // Buscar por nombre
        }
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // Manejar respuestas de error
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error en la conexión: " + connection.getResponseCode());
        }

        StringBuilder data;
        try (Scanner scanner = new Scanner(url.openStream())) {
            data = new StringBuilder();
            while (scanner.hasNext()) {
                data.append(scanner.nextLine());
            }
        }

        return data.toString();
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+"); // Verifica si el string contiene solo dígitos
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pokeIdentifier = request.getParameter("name");
        if (pokeIdentifier == null || pokeIdentifier.isEmpty()) {
            pokeIdentifier = "pikachu"; // Valor por defecto
        }

        try {
            String pokeData = getPokeData(pokeIdentifier);
            JsonObject json = JsonParser.parseString(pokeData).getAsJsonObject();

            // Extraer información relevante
            String name = json.get("name").getAsString();
            int id = json.get("id").getAsInt();
            String imageUrl = json.getAsJsonObject("sprites").get("front_default").getAsString();
            JsonArray types = json.getAsJsonArray("types");
            StringBuilder typeList = new StringBuilder();
            for (int i = 0; i < types.size(); i++) {
                typeList.append(types.get(i).getAsJsonObject().getAsJsonObject("type").get("name").getAsString()).append(", ");
            }
            String typesString = typeList.length() > 0 ? typeList.substring(0, typeList.length() - 2) : "N/A";

            // Generar HTML
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html><head><link rel='stylesheet' type='text/css' href='styles.css'></head><body style='background-color: #ffeb3b; color: #000000;'>");
            out.println("<div class='pokemon-info' style='text-align: center; margin: auto; width: 80%;'>");
            out.println("<h1>Pokémon: </h1>");
            out.println("<h1>" + name.toUpperCase() + "</h1>");
            out.println("<h1>Número de Pokémon: " + id + "</h1>");
            out.println("<img src='" + imageUrl + "' alt='" + name + "' style='width: 200px; height: auto;'>");
            out.println("<h3 class='types'>Tipo: " + typesString + "</h3>");

            // Agregar formulario para elegir otro Pokémon
            out.println("<form action='/poke' method='get' style='margin-top: 20px;'>");
            out.println("<label for='name'>Elige otro Pokémon o ingresa un ID:</label>");
            out.println("<input type='text' id='name' name='name' placeholder='Nombre o ID del Pokémon' required>");
            out.println("<button type='submit'>Buscar</button>");
            out.println("</form>");

            out.println("</div>");
            out.println("</body></html>");
        } catch (IOException e) {
            // Redirigir a la página de error
            response.sendRedirect("error.html");
        }
    }
}
