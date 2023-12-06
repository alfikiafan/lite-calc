import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Font DISPLAY_FONT = new Font("Segoe UI SemiBold", Font.PLAIN, 36);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font BACKSPACE_FONT = new Font("Segoe UI Regular", Font.PLAIN, 18);
    private static final Font DIVIDE_BY_ZERO_FONT = new Font("Segoe UI", Font.PLAIN, 24);
    private static final String DIVIDE_BY_ZERO_STRING = "Cannot divide by zero";
    private static final String DIVIDE_BY_ZERO_LINE = "([-+]?\\d*\\.?\\d+)\\s*÷\\s*0";

    private static final int BUTTONS_PER_ROW = 4;

    private boolean equalsButtonClicked = false;

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

        JPanel buttonPanel = new JPanel(new GridLayout(0, 4, 3, 3));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        String[] buttonLabels = {
                "%", "C", "⌫", "÷",
                "7", "8", "9", "×",
                "4", "5", "6", "+",
                "1", "2", "3", "-",
                ".", "0", "+/-", "="
        };

        JButton[] buttons = new JButton[buttonLabels.length];

        // Inisialisasi baris saat ini

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = createButton(buttonLabels[i]);

            // Adjust row based on the threshold
            gbc.gridy = i / BUTTONS_PER_ROW;
            buttonPanel.add(buttons[i], gbc);
        }


        panel.add(buttonPanel, BorderLayout.CENTER);

        add(panel, gbc);

        for (JButton button : buttons) {
            button.addActionListener(e -> {
                processButtonAction(button.getText());
                LiteCalc.this.requestFocusInWindow();
            });
            button.setFocusable(true);
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
        if (equalsButtonClicked) {
            currentInput.setLength(0);
            equalsButtonClicked = false;
        }
        if (text.equals(".") && currentInput.toString().contains(".")) {
            // Do not allow multiple decimal points
            return;
        }
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
        // if user enters zero after dot ("."), add the zero
        if (currentInput.length() > 1 && currentInput.charAt(currentInput.length() - 1) == '.' && currentInput.charAt(currentInput.length() - 2) != '0' && text.charAt(0) == '0') {
            currentInput.append(text);
        }

        // if user enter zero right after operator, then user enter another digit, remove the zero
        if (currentInput.length() > 1 && !(currentInput.charAt(currentInput.length() - 2) == '.') && !Character.isDigit(currentInput.charAt(currentInput.length() - 2)) && currentInput.charAt(currentInput.length() - 1) == '0' && Character.isDigit(text.charAt(0))) {
            currentInput.deleteCharAt(currentInput.length() - 1);
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
        handleKeyPress(e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Implementasi jika diperlukan
    }

    // Metode untuk menangani penekanan tombol pada keyboard
    private void handleKeyPress(char keyChar) {
        String keyText = String.valueOf(keyChar);

        // Cek apakah karakter adalah digit, operator, atau karakter khusus lainnya
        if (Character.isDigit(keyChar) || "+".indexOf(keyChar) != -1) {
            processButtonAction(keyText);
        } else if (keyChar == '\b') {
            removeLastCharacter();
        } else if (keyChar == '\n' || keyChar == '=') {
            calculateResult();
            equalsButtonClicked = true;
        } else if (keyChar == '%') {
            processOperator("%");
        } else if (keyChar == '.' || keyChar == ',') {
            appendToDisplay(".");
        } else if (keyChar == '\u007F') {
            clearDisplay();
        } else if (keyChar == '/' || keyChar == '\\') {
            processOperator("÷");
        } else if (keyChar == '*') {
            processOperator("×");
        } else if (keyChar == '-') {
            processOperator("-");
        } else if (keyChar == 't' || keyChar == 'T') {
            toggleSign();
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
            equalsButtonClicked = true;
        } else {
            Pattern pattern = Pattern.compile(DIVIDE_BY_ZERO_LINE);
            Matcher matcher = pattern.matcher(currentInput.toString());
            System.out.println(currentInput.toString());
            if (matcher.matches()) {
                System.out.println("division by zero");
                handleDivideByZero();
            } else {
                processOperator(buttonText);
            }
        }
    }

    private void processOperator(String op) {
        equalsButtonClicked = false;
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
    private void handleDivideByZero() {
        clearDisplay();
        display.setFont(DIVIDE_BY_ZERO_FONT);
        display.setText(DIVIDE_BY_ZERO_STRING);
        System.err.println("Error: Division by zero.");
    }

    private void removeLastCharacter() {
        if (currentInput.length() == 2 && currentInput.charAt(0) == '-') {
            currentInput.setLength(0);
            currentInput.insert(0, '0');
            currentInput.append("0");
        }

        // Jika panjang input adalah 1, set input ke 0
        if (currentInput.length() == 1) {
            currentInput.setLength(0);
            currentInput.insert(0, '0');
            currentInput.append("0");
        }

        // if the last character is the result of division by zero, clear the display
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
                currentOperator = null;
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
                    resultHandler(result);
                }
            }
        }
    }

    private void resultHandler(BigDecimal result) {
        display.setText(result.stripTrailingZeros().toPlainString());
        currentInput.setLength(0);
        currentInput.append(result.stripTrailingZeros().toPlainString());
        currentOperator = null;
    }

    private BigDecimal performOperation(BigDecimal num1, BigDecimal num2, String op) {
        switch (op) {
            case "÷":
                if (!num2.equals(BigDecimal.ZERO)) {
                    return num1.divide(num2, 10, RoundingMode.HALF_UP);
                } else {
                    handleDivideByZero();
                    return null;
                }
            case "+":
                return num1.add(num2);
            case "-":
                return num1.subtract(num2);
            case "×":
                return num1.multiply(num2);
            default:
                return BigDecimal.ZERO;
        }
    }

    private void clearDisplay() {
        currentInput.setLength(0);
        currentOperator = null;
        equalsButtonClicked = false;
        display.setText("0");
    }

    private void applyPercentage() {
        if (!currentInput.isEmpty()) {
            BigDecimal number = new BigDecimal(currentInput.toString());
            BigDecimal result = number.divide(BigDecimal.valueOf(100.0), 10, RoundingMode.HALF_UP);

            resultHandler(result);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LiteCalc liteCalc = new LiteCalc();
            liteCalc.setVisible(true);
            liteCalc.requestFocus();
        });
    }
}