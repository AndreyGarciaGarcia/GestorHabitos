package com.example.gestionPlanes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ConexionBBDD {

    //Datos para la conexion a la bbdd
    private static final String URL = "jdbc:mysql://localhost:3306/gestion?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            //MENU DE OPCIONES
            System.out.println("\nMENU DE LA GESTION DE PLANES:");
            System.out.println("1. Consultar Usuarios");
            System.out.println("2. Consultar Hábitos");
            System.out.println("3. Añadir Usuario");
            System.out.println("4. Añadir Hábito");
            System.out.println("5. Salir");
            System.out.print("Elige una opción: ");
            
            //Declaramos una variable de tipo entero sin inicializar
            int opcion;
            
            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingresa un número válido.");
                continue;
            }

            //Usamos el switch para las distintas opciones
            switch (opcion) {
                case 1:
                    consultarUsuarios();
                    break;
                case 2:
                    consultarHabitos();
                    break;
                case 3:
                    añadirUsuario(scanner);
                    break;
                case 4:
                    añadirHabito(scanner);
                    break;
                case 5:
                    salir = true;
                    System.out.println("SALIR");
                    break;
                default:
                    System.out.println("La opcion no es valida. elige una entre 1 y 5");
            }
        }
        scanner.close();
    }

    // Método para consultar usuarios
    private static void consultarUsuarios() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM usuarios")) {

            System.out.println("Usuarios:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String email = resultSet.getString("email");
                int planId = resultSet.getInt("plan_id");
                System.out.println("ID: " + id + ", Nombre: " + nombre + ", Email: " + email + ", Plan ID: " + planId);
            }
        } catch (SQLException e) {
            System.out.println("ERROR al consultar usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para consultar HABITOS
    private static void consultarHabitos() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM habitos")) {

            System.out.println("\nHábitos:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int usuarioId = resultSet.getInt("usuario_id");
                String nombre = resultSet.getString("nombre");
                String frecuencia = resultSet.getString("frecuencia");
                System.out.println("ID: " + id + ", Usuario ID: " + usuarioId + ", Nombre: " + nombre + ", Frecuencia: " + frecuencia);
            }
        } catch (SQLException e) {
            System.out.println("ERROR al consultar hábitos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para añadir un USUARIOS
    private static void añadirUsuario(Scanner scanner) {
        System.out.println("\nAñadir Nuevo Usuario:");
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.println("Elige un plan:");
        System.out.println("1. Premium");
        System.out.println("2. Estándar");
        System.out.print("Opción: ");
        
        //Variable de tipo entro no inicializada
        int planId;
        
        try {
            planId = Integer.parseInt(scanner.nextLine());
            if (planId != 1 && planId != 2) {
                System.out.println("Plan no válido. Debe ser 1 (Premium) o 2 (Estándar).");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("El Plan ID debe ser un número.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO usuarios (nombre, email, plan_id) VALUES (?, ?, ?)")) {
            statement.setString(1, nombre);
            statement.setString(2, email);
            statement.setInt(3, planId);
            statement.executeUpdate();
            System.out.println("Usuario añadido correctamente.");
        } catch (SQLException e) {
            System.out.println("ERROR al añadir usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para añadir un hábito
    private static void añadirHabito(Scanner scanner) {
        System.out.println("\nAñadir Nuevo Hábito:");
        System.out.print("ID del Usuario: ");
        int usuarioId;
        try {
            usuarioId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("El ID del usuario debe ser un número.");
            return;
        }
        System.out.print("Nombre del Hábito: ");
        String nombre = scanner.nextLine();
        System.out.println("Elige una frecuencia:");
        System.out.println("1. Diario");
        System.out.println("2. Semanal");
        System.out.println("3. Mensual");
        System.out.print("Opción: ");
        String frecuencia;
        try {
            int frecuenciaOpcion = Integer.parseInt(scanner.nextLine());
            switch (frecuenciaOpcion) {
                case 1:
                    frecuencia = "Diario";
                    break;
                case 2:
                    frecuencia = "Semanal";
                    break;
                case 3:
                    frecuencia = "Mensual";
                    break;
                default:
                    System.out.println("Frecuencia no válida. Debe ser 1 (Diario), 2 (Semanal) o 3 (Mensual).");
                    return;
            }
        } catch (NumberFormatException e) {
            System.out.println("La opción de frecuencia debe ser un número.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO habitos (usuario_id, nombre, frecuencia) VALUES (?, ?, ?)")) {
            statement.setInt(1, usuarioId);
            statement.setString(2, nombre);
            statement.setString(3, frecuencia);
            statement.executeUpdate();
            System.out.println("Hábito añadido correctamente.");
        } catch (SQLException e) {
            System.out.println("ERROR al añadir hábito: " + e.getMessage());
            e.printStackTrace();
        }
    }
}