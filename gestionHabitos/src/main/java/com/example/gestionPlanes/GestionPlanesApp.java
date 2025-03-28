package com.example.gestionPlanes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class GestionPlanesApp extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private JTable usuariosTable;
    private JTable habitosTable;
    private DefaultTableModel usuariosModel;
    private DefaultTableModel habitosModel;
    private Map<String, Integer> planesMap; // Para mapear nombres de planes a sus IDs
    private Map<String, Integer> usuariosMap; // Para mapear nombres de usuarios a sus IDs

    public GestionPlanesApp() {
        // Configuración del JFrame
        setTitle("Gestión de Planes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar los mapas
        planesMap = new HashMap<>();
        usuariosMap = new HashMap<>();

        // Cargar planes y usuarios al iniciar
        cargarPlanes();
        cargarUsuariosMap();

        // Crear un JTabbedPane para las pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        //USUARIOS
        JPanel usuariosPanel = new JPanel(new BorderLayout());
        usuariosModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Email", "Plan ID"}, 0);
        usuariosTable = new JTable(usuariosModel);
        usuariosPanel.add(new JScrollPane(usuariosTable), BorderLayout.CENTER);

        JButton addUsuarioButton = new JButton("Añadir Usuario");
        addUsuarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                añadirUsuario();
            }
        });
        usuariosPanel.add(addUsuarioButton, BorderLayout.SOUTH);

        // Pestaña 2: Hábitos
        JPanel habitosPanel = new JPanel(new BorderLayout());
        habitosModel = new DefaultTableModel(new String[]{"ID", "Usuario ID", "Nombre", "Frecuencia"}, 0);
        habitosTable = new JTable(habitosModel);
        habitosPanel.add(new JScrollPane(habitosTable), BorderLayout.CENTER);

        JButton addHabitoButton = new JButton("Añadir Hábito");
        addHabitoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                añadirHabito();
            }
        });
        habitosPanel.add(addHabitoButton, BorderLayout.SOUTH);

        tabbedPane.addTab("Usuarios", usuariosPanel);
        tabbedPane.addTab("Hábitos", habitosPanel);

        add(tabbedPane);

        cargarUsuarios();
        cargarHabitos();
    }

    // Método para cargar los planes desde la base de datos
    private void cargarPlanes() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, tipo FROM planes_suscripcion")) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String tipo = resultSet.getString("tipo");
                planesMap.put(tipo, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar planes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cargar los usuarios (para el JComboBox)
    private void cargarUsuariosMap() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, nombre FROM usuarios")) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                usuariosMap.put(nombre, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios para el mapa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cargar datos de la tabla usuarios
    private void cargarUsuarios() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM usuarios")) {

            usuariosModel.setRowCount(0);
            while (resultSet.next()) {
                usuariosModel.addRow(new Object[]{
                    resultSet.getInt("id"),
                    resultSet.getString("nombre"),
                    resultSet.getString("email"),
                    resultSet.getInt("plan_id")
                });
            }
            // Actualizar el mapa de usuarios después de cargar
            cargarUsuariosMap();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cargar datos de la tabla habitos
    private void cargarHabitos() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM habitos")) {

            habitosModel.setRowCount(0);
            while (resultSet.next()) {
                habitosModel.addRow(new Object[]{
                    resultSet.getInt("id"),
                    resultSet.getInt("usuario_id"),
                    resultSet.getString("nombre"),
                    resultSet.getString("frecuencia")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar hábitos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para añadir un nuevo usuario
    private void añadirUsuario() {
        JTextField nombreField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JComboBox<String> planComboBox = new JComboBox<>(planesMap.keySet().toArray(new String[0]));

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Plan:"));
        panel.add(planComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText();
            String email = emailField.getText();
            String planSeleccionado = (String) planComboBox.getSelectedItem();
            int planId = planesMap.get(planSeleccionado);

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO usuarios (nombre, email, plan_id) VALUES (?, ?, ?)")) {
                statement.setString(1, nombre);
                statement.setString(2, email);
                statement.setInt(3, planId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usuario añadido correctamente.");
                cargarUsuarios(); // Actualizar la tabla y el mapa de usuarios
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al añadir usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para añadir un nuevo hábito
    private void añadirHabito() {
        JComboBox<String> usuarioComboBox = new JComboBox<>(usuariosMap.keySet().toArray(new String[0]));
        JTextField nombreField = new JTextField(20);
        String[] frecuencias = {"Diario", "Semanal", "Mensual"};
        JComboBox<String> frecuenciaComboBox = new JComboBox<>(frecuencias);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Usuario:"));
        panel.add(usuarioComboBox);
        panel.add(new JLabel("Nombre del Hábito:"));
        panel.add(nombreField);
        panel.add(new JLabel("Frecuencia:"));
        panel.add(frecuenciaComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Nuevo Hábito", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String usuarioSeleccionado = (String) usuarioComboBox.getSelectedItem();
            int usuarioId = usuariosMap.get(usuarioSeleccionado);
            String nombre = nombreField.getText();
            String frecuencia = (String) frecuenciaComboBox.getSelectedItem();

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO habitos (usuario_id, nombre, frecuencia) VALUES (?, ?, ?)")) {
                statement.setInt(1, usuarioId);
                statement.setString(2, nombre);
                statement.setString(3, frecuencia);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Hábito añadido correctamente.");
                cargarHabitos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al añadir hábito: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GestionPlanesApp().setVisible(true);
        });
    }
}
