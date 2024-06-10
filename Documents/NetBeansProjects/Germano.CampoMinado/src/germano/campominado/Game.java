package germano.campominado;

import germano.sounds.SoundController;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JOptionPane;

public class Game extends javax.swing.JFrame {

    MineTile[][] board;                 // Matriz com 2 dimensões para posicionamento dos botões
    MineTile tile;                      // Botão
    ArrayList<MineTile> mineList;       // Lista de minas(Quantidade flexível de valores)
    GameSettings gameSettings;          // Configurações do jogo
    Timer timer;                        // Cronômetro
    TimerTask task;                     // Tarefa que será executada no cronômetro
    MainMenu mainMenu;                  // Referência ao menu principal
    SoundController soundController;    // Controladora de audio
    ScoreManager scoreManager;          // Controladora de placar
    JOptionPane jpane;                  // Menus
    Object[] options = {"Placar", "Jogar novamente"}; // Opções do menu

    public Game(GameSettings gameSettings, ScoreManager scoreManager) {
        // Atribuí valores as variáveis
        board = new MineTile[gameSettings.nRows][gameSettings.nColumns];
        this.gameSettings = gameSettings;
        this.scoreManager = scoreManager;
        jpane = new JOptionPane();
        soundController = new SoundController();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                int minutes = Math.floorDiv(gameSettings.getTimePassed(), 60);
                int seconds = gameSettings.getTimePassed() % 60;
                jLabel_Timer.setText(String.format("%02d:%02d", minutes, seconds));
                gameSettings.setTimePassed(gameSettings.getTimePassed() + 1);
            }
        };

        // Inicia o "front"
        initComponents();
        this.setVisible(true);

        // Coloquei o timer para iniciar somente após a visibilidade do "front"
        timer.scheduleAtFixedRate(task, 0, 1000);

        // Monta o tabuleiro
        for (int row = 0; row < gameSettings.nRows; row++) {
            for (int column = 0; column < gameSettings.nColumns; column++) {
                tile = new MineTile(row, column);
                board[row][column] = tile;
                jPanel_board.add(tile);
            }
        }

        setMines();
    }

    // Define a posição das bombas no começõ do jogo
    private void setMines() {
        mineList = new ArrayList<>();
        Random random = new Random();
        int rowPosition;
        int columnPosition;
        while (mineList.size() < gameSettings.getnMines()) {
            rowPosition = random.nextInt(gameSettings.nRows);
            columnPosition = random.nextInt(gameSettings.nColumns);
            if (!mineList.contains(board[rowPosition][columnPosition])) {
                mineList.add(board[rowPosition][columnPosition]);
            }
        }
        jLabel_MineCounter.setText(String.format("Minas faltantes: %02d", gameSettings.getnMines()));
    }

    // Revela a posição das bombas ao clicar em uma
    private void revealMines() {
        for (int i = 0; i < mineList.size(); i++) {
            mineList.get(i).setText("💣");
            mineList.get(i).setForeground(Color.red);
        }
    }

    // Valida ao redor da bomba em caso de acerto
    private void checkAround(MineTile tile) {
        soundController.playRegularClickSound();
        gameSettings.setScore();
        if (countMinesAround(tile) > 0) {
            tile.setText(String.valueOf(countMinesAround(tile)));
        } else {
            // Para cada linha superior, atual e inferior
            for (int row = -1; row <= 1; row++) {
                // Se a linha tá dentro do range
                if (row + tile.row >= 0 && row + tile.row < gameSettings.nRows) {
                    // Para cada coluna esquerda, atual e direita
                    for (int column = -1; column <= 1; column++) {
                        // Se a coluna está dentro do range
                        if (column + tile.column >= 0 && column + tile.column < gameSettings.nColumns) {
                            if (board[row + tile.row][column + tile.column].isEnabled()) {
                                board[row + tile.row][column + tile.column].setEnabled(false);
                                checkAround(board[row + tile.row][column + tile.column]);
                            }
                        }
                    }
                }
            }
        }
    }

    // Conta a quantidade de minas ao redor
    private int countMinesAround(MineTile tile) {
        int n = 0;
        // Para cada linha superior, atual e inferior
        for (int row = -1; row <= 1; row++) {
            // Se a linha tá dentro do range
            if (row + tile.row >= 0 && row + tile.row < gameSettings.nRows) {
                // Para cada coluna esquerda, atual e direita
                for (int column = -1; column <= 1; column++) {
                    // Se a coluna está dentro do range
                    if (column + tile.column >= 0 && column + tile.column < gameSettings.nColumns) {
                        // Valida se este tile está lista de minas
                        if (mineList.contains(board[row + tile.row][column + tile.column])) {
                            n += 1;
                        }
                    }
                }
            }
        }
        return n;
    }

    // Classe do botão
    private class MineTile extends JButton {

        int row;
        int column;

        public MineTile(int row, int column) {
            this.row = row;
            this.column = column;
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    tile = (MineTile) e.getSource();

                    switch (e.getButton()) {
                        // Clique esquerdo
                        case MouseEvent.BUTTON1 -> {
                            // Se é um quadro vazio
                            if (tile.getText() == "") {
                                // Se é um quadro que está na lista de bombas
                                if (mineList.contains(tile)) {
                                    soundController.playExplosionSound();
                                    revealMines();
                                    task.cancel();
                                    gameSettings.setGameOver();
                                    setGameFinished(false);
                                } // Se é um quadro que não está na lista de bombas
                                else {
                                    tile.setEnabled(false);
                                    checkAround(tile);
                                    validateVictory();
                                }
                            }
                        }

                        // Clique direito
                        case MouseEvent.BUTTON3 -> {
                            if (tile.isEnabled() && tile.getText() != "💣") {
                                switch (tile.getText()) {
                                    case "" ->
                                        tile.setText("🚩");
                                    case "🚩" ->
                                        tile.setText("");
                                    default ->
                                        throw new AssertionError();
                                }
                                validateVictory();
                            }
                        }
                        default ->
                            throw new AssertionError();
                    }
                }

            });
        }

    }

    private void returnMainMenu() {
        mainMenu = new MainMenu();
        this.setVisible(false);
        mainMenu.setVisible(true);
    }

    // Valida vitória
    private void validateVictory() {
        if (gameSettings.getScore() >= (gameSettings.getNtiles()) - gameSettings.getnMines()) {
            gameSettings.setGameWon();
            setGameFinished(true);
        }
    }

    private void setGameFinished(boolean isGameWon) throws HeadlessException, AssertionError {
        if (isGameWon == true) {
            while (true) {
                String n;
                try {
                    n = jpane.showInputDialog(null,
                            "Você ganhou!"
                            + "\nPontuação: " + gameSettings.getScore()
                            + "\nJogador, qual seu nome?",
                            "Partida concluída",
                            MessageType.NONE.ordinal());
                    if (n == null) {
                        throw new Exception();
                    } else {
                        gameSettings.setPlayerName(n);
                        break;
                    }
                } catch (Exception x) {
                    jpane.showMessageDialog(null, "Valor inválido.");
                }

            }
        } else {
            while (true) {
                String n;
                try {
                    n = jpane.showInputDialog(null,
                            "Você perdeu!"
                            + "\nPontuação: " + gameSettings.getScore()
                            + "\nJogador, qual seu nome?",
                            "Partida concluída",
                            MessageType.NONE.ordinal());
                    if (n == null) {
                        throw new Exception();
                    } else {
                        gameSettings.setPlayerName(n);
                        break;
                    }
                } catch (Exception x) {
                    jpane.showMessageDialog(null, "Valor inválido.");
                }

            }
        }

        int choice = jpane.showOptionDialog(null,
                "Deseja acessar o placar ou jogar novamente?",
                "Partida concluída!",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        scoreManager.addGame(gameSettings);

        switch (choice) {
            case 0 -> {
                jpane.showMessageDialog(null, "Menu de placar em construção!");
                returnMainMenu();
            }
            case 1 -> {
                returnMainMenu();
            }
            default ->
                throw new AssertionError();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_header = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel_MineCounter = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel_Timer = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel_board = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Projeto A3 - Campo minado");
        setBackground(javax.swing.UIManager.getDefaults().getColor("nb.multitabs.background"));
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        setResizable(false);
        setSize(getPreferredSize());

        jPanel_header.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Blue"));

        jButton1.setText("Voltar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel_MineCounter.setText("Minas faltantes: XX");

        jLabel2.setText("Tempo corrido:");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel_Timer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Timer.setText("00:00");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel_headerLayout = new javax.swing.GroupLayout(jPanel_header);
        jPanel_header.setLayout(jPanel_headerLayout);
        jPanel_headerLayout.setHorizontalGroup(
            jPanel_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_headerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Timer, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_MineCounter)
                .addContainerGap())
        );
        jPanel_headerLayout.setVerticalGroup(
            jPanel_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel_headerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_MineCounter)
                    .addGroup(jPanel_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel_Timer)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel_board.setLayout(new java.awt.GridLayout(MainMenu.gameSettings.nRows, MainMenu.gameSettings.nColumns));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel_board, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel_header, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel_header, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_board, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        returnMainMenu();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel_MineCounter;
    public javax.swing.JLabel jLabel_Timer;
    private javax.swing.JPanel jPanel_board;
    private javax.swing.JPanel jPanel_header;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
}
