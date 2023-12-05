import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LiteCalc extends JFrame implements KeyListener {
    private JTextField display;
    private StringBuilder currentInput;
    private String currentOperator;

    private static final Color BUTTON_BACKGROUND_COLOR = Color.decode("#F9F9F9");
    private static final Color EQUALS_BUTTON_BACKGROUND_COLOR = Color.decode("#0055A1");
    private static final Color BACKGROUND_COLOR = Color.decode("#F3F3F3");
    private static final Color OUTLINE_BORDER_COLOR = Color.decode("#E6E5E2");

    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 400;
    private static final int DISPLAY_HEIGHT = 84;

    private static final Font DISPLAY_FONT = new Font("Segoe UI", Font.PLAIN, 36);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font BACKSPACE_FONT = new Font("Segoe UI Regular", Font.PLAIN, 18);
    private static final Font DIVIDE_BY_ZERO_FONT = new Font("Segoe UI", Font.PLAIN, 24);
    private static final String DIVIDE_BY_ZERO_STRING = "Cannot divide by zero";

    public LiteCalc() {
        setTitle("LiteCalc");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        initComponents();
        addKeyListener(this);
        setFocusable(true);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        display = new JTextField();
        display.setFont(DISPLAY_FONT);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setEditable(false);
        display.setBackground(BACKGROUND_COLOR);
        display.setPreferredSize(new Dimension(display.getPreferredSize().width, DISPLAY_HEIGHT));
        display.setText("0");
        panel.add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 4, 5, 2));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        String[] buttonLabels = {
                "%", "C", "⌫", "÷",
                "7", "8", "9", "×",
                "4", "5", "6", "+",
                "1", "2", "3", "-",
                ".", "0", "+/-", "="
        };

        JButton[] buttons = new JButton[buttonLabels.length];

        int row = 0; // Inisialisasi baris saat ini

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = createButton(buttonLabels[i]);

            // Atur bobot berbeda pada setiap baris
            gbc.gridy = row;
            buttonPanel.add(buttons[i], gbc);

            if (i == 3 || i == 7 || i == 11 || i == 15 || i == 18) {
                row++; // Pindah ke baris berikutnya setelah 4 tombol
            }
        }

        panel.add(buttonPanel, BorderLayout.CENTER);

        add(panel, gbc);

        for (JButton button : buttons) {
            button.addActionListener(e -> processButtonAction(button.getText()));
        }

        currentInput = new StringBuilder();
        currentOperator = null;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(text.equals("⌫") ? BACKSPACE_FONT : BUTTON_FONT);
        button.setFocusPainted(false);

        if (text.equals("=")) {
            setButtonStyle(button, EQUALS_BUTTON_BACKGROUND_COLOR, Color.WHITE);
        } else {
            setButtonStyle(button, BUTTON_BACKGROUND_COLOR, null);
        }

        return button;
    }

    private void setButtonStyle(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createLineBorder(OUTLINE_BORDER_COLOR, 1));
    }

    private void appendToDisplay(String text) {
        if (text.equals(".") && currentInput.isEmpty()) {
            currentInput.append("0");
        } else if (text.equals(".") && !currentInput.isEmpty()) {
            int lastIndex = currentInput.length() - 1;
            char lastChar = currentInput.charAt(lastIndex);

            if (!Character.isDigit(lastChar)) {
                currentInput.append("0");
            }
        }

        // Handle the case where the first digit is 0
        if (currentInput.toString().equals("0") && Character.isDigit(text.charAt(0))) {
            currentInput.setLength(0);
        }

        currentInput.append(text);
        display.setText(currentInput.toString());
    }
    // Implementasi metode-metode dari KeyListener
    @Override
    public void keyTyped(KeyEvent e) {
        // Implementasi jika diperlukan
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKeyPress(e.getKeyChar(), e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Implementasi jika diperlukan
    }

    // Metode untuk menangani penekanan tombol pada keyboard
    private void handleKeyPress(char keyChar, int keyCode) {
        String keyText = String.valueOf(keyChar);

        // Cek apakah karakter adalah digit, operator, atau karakter khusus lainnya
        if (Character.isDigit(keyChar) || "+-*/".indexOf(keyChar) != -1) {
            processButtonAction(keyText);
        } else if (keyChar == '\b') {
            removeLastCharacter();
        } else if (keyChar == '\n' || keyChar == '=') {
            calculateResult();
        } else if (keyChar == '%') {
            processOperator("%");
        } else if (keyChar == '.') {
            appendToDisplay(keyText);
        } else if (keyCode == KeyEvent.VK_DELETE ) {
            clearDisplay();
        }
    }
    private void processButtonAction(String buttonText) {
        if (!currentInput.toString().equals(DIVIDE_BY_ZERO_STRING)) {
            display.setFont(DISPLAY_FONT);
        }
        if (buttonText.equals("C")) {
            clearDisplay();
        } else if (buttonText.equals("⌫")) {
            removeLastCharacter();
        } else if (buttonText.equals("+/-")) {
            toggleSign();
        } else if (Character.isDigit(buttonText.charAt(0)) || buttonText.equals(".")) {
            appendToDisplay(buttonText);
        } else if (buttonText.equals("=")) {
            calculateResult();
        } else {
            processOperator(buttonText);
        }
    }

    private void processOperator(String op) {
        if (currentInput.isEmpty()) {
            if (op.equals(".")) {
                appendToDisplay("0.");
            } else {
                currentOperator = op;
                currentInput.append("0 ").append(op).append(" ");
            }
        } else if (!Character.isDigit(currentInput.charAt(currentInput.length() - 1))) {
            // Hapus karakter terakhir jika itu operator dan ganti dengan operator baru
            if (currentInput.length() > 1 && !Character.isDigit(currentInput.charAt(currentInput.length() - 2))) {
                currentInput.deleteCharAt(currentInput.length() - 2);
            }
            currentOperator = op;
            // Perbarui operator tanpa menambahkan spasi setelahnya
            currentInput.deleteCharAt(currentInput.length() - 1);
            currentInput.append(op).append(" ");
        } else {
            if (currentOperator != null) {
                calculateResult();
            }
            if (op.equals("-") && currentInput.isEmpty()) {
                appendToDisplay(op);
            } else if (op.equals("%")) {
                applyPercentage();
            } else {
                currentOperator = op;
                currentInput.append(" ").append(op).append(" ");
            }
        }
        display.setText(currentInput.toString());
    }

    private void removeLastCharacter() {
        if (display.getText().equals(DIVIDE_BY_ZERO_STRING)) {
            clearDisplay();
        }
        if (!currentInput.isEmpty()) {
            int lastIndex = currentInput.length() - 1;

            // Hapus satu karakter dari StringBuilder jika karakter terakhir adalah digit
            if (Character.isDigit(currentInput.charAt(lastIndex))) {
                currentInput.deleteCharAt(lastIndex);
            } else {
                // Jika karakter terakhir bukan digit, hapus semua karakter sampai menemukan digit
                while (lastIndex >= 0 && !Character.isDigit(currentInput.charAt(lastIndex))) {
                    currentInput.deleteCharAt(lastIndex);
                    lastIndex--;
                }
            }
            display.setText(currentInput.toString());
        }
    }

    private void toggleSign() {
        if (!currentInput.isEmpty()) {
            if (currentOperator == null) {
                // Jika tidak ada operator, ubah tanda pada bilangan utama
                if (currentInput.charAt(0) == '-') {
                    currentInput.deleteCharAt(0);
                } else {
                    currentInput.insert(0, '-');
                }
            } else {
                // Jika ada operator, ubah tanda pada operand kedua jika ada
                String[] expression = currentInput.toString().split("\\s+");
                if (expression.length == 3) {
                    String secondOperand = expression[2];
                    if (secondOperand.startsWith("(-") && secondOperand.endsWith(")")) {
                        // Operand kedua adalah bilangan negatif dalam kurung
                        expression[2] = secondOperand.substring(2, secondOperand.length() - 1);
                    } else if (Character.isDigit(secondOperand.charAt(0)) || (secondOperand.length() > 1 && secondOperand.charAt(0) == '-' && Character.isDigit(secondOperand.charAt(1)))) {
                        // Check if the second operand is a digit or starts with a negative sign followed by a digit
                        expression[2] = "(-" + secondOperand + ")";
                    }
                    currentInput.setLength(0);
                    for (String part : expression) {
                        currentInput.append(part).append(" ");
                    }
                    currentInput.deleteCharAt(currentInput.length() - 1); // Remove trailing space
                }
            }
            display.setText(currentInput.toString());
        }
    }

    private void calculateResult() {
        if (currentOperator != null) {
            String[] expression = currentInput.toString().split("\\s+");
            if (expression.length == 3) {
                BigDecimal num1;
                BigDecimal num2;

                if (expression[0].startsWith("(-") && expression[0].endsWith(")")) {
                    // Operand pertama adalah bilangan negatif dalam kurung
                    num1 = new BigDecimal(expression[0].substring(2, expression[0].length() - 1)).negate();
                } else {
                    num1 = new BigDecimal(expression[0]);
                }

                String operator = expression[1];

                if (expression[2].startsWith("(-") && expression[2].endsWith(")")) {
                    // Operand kedua adalah bilangan negatif dalam kurung
                    num2 = new BigDecimal(expression[2].substring(2, expression[2].length() - 1)).negate();
                } else {
                    num2 = new BigDecimal(expression[2]);
                }

                BigDecimal result = performOperation(num1, num2, operator);

                if (result != null) {
                    display.setText(result.stripTrailingZeros().toPlainString());
                    currentInput.setLength(0);

                    // Set hasil perhitungan ke currentInput
                    currentInput.append(result.stripTrailingZeros().toPlainString());

                    // Reset operator
                    currentOperator = null;
                }
            }
        }
    }

    private BigDecimal performOperation(BigDecimal num1, BigDecimal num2, String op) {
        switch (op) {
            case "+":
                return num1.add(num2);
            case "-":
                return num1.subtract(num2);
            case "×":
                return num1.multiply(num2);
            case "÷":
                if (!num2.equals(BigDecimal.ZERO)) {
                    return num1.divide(num2, 10, RoundingMode.HALF_UP);
                } else {
                    display.setFont(DIVIDE_BY_ZERO_FONT);
                    display.setText(DIVIDE_BY_ZERO_STRING);
                    currentInput.setLength(0);
                    return null;
                }
            default:
                return BigDecimal.ZERO;
        }
    }

    private void clearDisplay() {
        currentInput.setLength(0);
        currentOperator = null;
        display.setText("0");
    }

    private void applyPercentage() {
        if (!currentInput.isEmpty()) {
            BigDecimal number = new BigDecimal(currentInput.toString());
            BigDecimal result = number.divide(BigDecimal.valueOf(100.0), 10, RoundingMode.HALF_UP);

            display.setText(result.stripTrailingZeros().toPlainString());
            currentInput.setLength(0);
            currentInput.append(result.stripTrailingZeros().toPlainString());
            currentOperator = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LiteCalc liteCalc = new LiteCalc();
            liteCalc.setVisible(true);
        });
    }
}