package com.cns.plugin_clisitef;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import br.com.softwareexpress.sitef.android.CliSiTefI;

public class XActivity extends Activity {
    private static final int CAMPO_COMPROVANTE_CLIENTE = 121;
    private static final int CAMPO_COMPROVANTE_ESTAB = 122;

    private class RequestCode
    {
        private static final int VALOR = 0;
        private static final int COLETA = 1;
    };

    //-- Configurações...
    private static String sitefIp;
    private static String sitefEmpresa;
    private static String sitefTerminal;
    private static String sitefParametros;

    //-- Venda...
    private static Integer sitefCupomFiscal;
    private static String sitefData;
    private static String sitefHora;
    private static String sitefRestricoes;
    private static String sitefOperador;

    private static boolean rodando = false;
    private static boolean espera = true;
    private static String mensagemVisor;
    private static int modalidade;
    private static CliSiTefI clisitef;
    private static Thread processoI = null;
    private static XActivity instance;
    private static boolean sitefTrOK;
    private static String statusTrn = "";
    private static String statusCnx = "";
    private static String package_name = "";
    private static Resources ResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Depreciado...
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        package_name = getApplication().getPackageName();
        ResourceId = getApplication().getResources();
        setContentView(ResourceId.getIdentifier("activity_x", "layout", package_name));
        //setContentView(R.layout.activity_x);

        Button btn = (Button) findViewById (ResourceId.getIdentifier("btCfgCancela", "id", package_name));
        //Button btn = (Button) findViewById (R.id.btCfgCancela);

        btn.setText("Cancelar");
        btn.setOnClickListener (new View.OnClickListener()
        {
            public void onClick (View v)
            {
                cancelaProcesso();
                setRetornoTela(false, "", true);
            }
        });

        instance = this;

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Double sitefValor;
        int retSitef = 0;
        String statusTrn = "";
        String statusCnx = "";

        //-- Instanciar o objeto CliSitef...
        if (clisitef == null) {
            try {
                clisitef = new CliSiTefI(this.getApplicationContext());
                clisitef.setDebug(false);

                //-- Configurações...
                sitefIp = getIntent().getExtras().getString("sitefIp", "");
                sitefEmpresa = getIntent().getExtras().getString("sitefEmpresa", "");
                sitefTerminal = getIntent().getExtras().getString("sitefTerminal", "");
                sitefParametros = getIntent().getExtras().getString("sitefParametros", "[TipoPinPad=ANDROID_BT;]");

                retSitef = clisitef.configuraIntSiTefInterativoEx(sitefIp, sitefEmpresa, sitefTerminal, sitefParametros);

                if (retSitef != 0) {
                    setRetornoTela(false, "Erro [ConfiguraIntSiTefInterativo]: " + retSitef, false);
                }

            } catch (Exception e) {
                retSitef = -1;
                setRetornoTela(false, "Erro inicialização: " + e.getMessage(), false);
            }

        }

        //--  Vendas...
        if ( retSitef == 0 ) {

            sitefValor = getIntent().getExtras().getDouble("sitefValor", 0.00);
            sitefCupomFiscal = getIntent().getExtras().getInt("sitefCupomFiscal", 0);
            sitefData = getIntent().getExtras().getString("sitefData", "");
            sitefHora = getIntent().getExtras().getString("sitefHora", "");
            if (sitefData.isEmpty()) {
                Calendar cal = new GregorianCalendar();
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                sitefData = sdf1.format(cal.getTime());
                SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmss", Locale.getDefault());
                sitefHora = sdf2.format(cal.getTime());
            }

            sitefOperador = getIntent().getExtras().getString("sitefOperador", "");
            sitefRestricoes = getIntent().getExtras().getString("sitefRestricoes", "");

            try {
                clisitef = CliSiTefI.getInstance();
                clisitef.setMessageHandler(hndMessage);

                //Chamar o Thread...
                if (processoI == null) {
                    //-- 0=Venda...
                    modalidade = 0;

                    executaTrn(modalidade,
                            sitefValor,
                            sitefCupomFiscal,
                            sitefData,
                            sitefHora,
                            sitefOperador,
                            sitefRestricoes);

                }

            } catch (Exception e) {
                setRetornoTela(false, "Erro na venda: " + e.getMessage(), false);
            }

        }
    }

    @Override
    public void onBackPressed() {
        //-- Desligar o botão back...
        //cancelaProcesso();
        //setRetornoTela(false, "Cancelado pelo botão voltar...", true);
    }

    private void executaTrn(int funcao, Double valor, Integer cupomFiscal, String dataCupom, String horaCupom, String operador, String restricoes)
    {

        mensagemVisor = "";
        setStatusTransacao("");
        cancelaProcesso ();
        //instance.setTitle ("Iniciando TEF");
        instance.setStatusConexao("Iniciando TEF");

        //-- Forçar duas casas e formatar string sem ponto...
        DecimalFormat formato = new DecimalFormat("#.00");
        String valorAux = formato.format(valor).replace(",","");

        int sts = clisitef.iniciaFuncaoSiTefInterativo(funcao,
                                                       valorAux,
                                                       cupomFiscal.toString(),
                                                       dataCupom, horaCupom, operador, restricoes);
        if (sts == 10000)
        {
            sitefTrOK = false;

            processoI = new Thread ()
            {
                // Aguarda até o fim de uma atividade de coleta
                private void esperaFimColeta () throws InterruptedException
                {
                    synchronized (this)
                    {
                        while (espera)
                            wait ();
                    }
                }

                public void run ()
                {
                    String messageErro;
                    try
                    {
                        int sts;
                        int proximoComando;

                        rodando = true;
                        do
                        {
                            sts = clisitef.continuaFuncaoSiTefInterativo ();
                            if (sts == 10000)
                            {
                                proximoComando = clisitef.getProximoComando ();
                                espera = true;
                                hndComando.sendEmptyMessage (proximoComando);
                                esperaFimColeta ();
                            }
                        }
                        while (rodando && sts == 10000);

                        if (sts == 0)
                        {
                            // deve confirmar (ou nao) a transacao
                            sts = clisitef.finalizaTransacaoSiTefInterativoEx (1, sitefCupomFiscal.toString(), sitefData, sitefHora, "");
                            while (rodando && sts == 10000)
                            {
                                sts = clisitef.continuaFuncaoSiTefInterativo ();
                                if (sts == 10000)
                                {
                                    proximoComando = clisitef.getProximoComando ();
                                    espera = true;
                                    hndComando.sendEmptyMessage (proximoComando);
                                    esperaFimColeta ();
                                }
                            }
                        }
                        else
                            hndTela.sendEmptyMessage(sts);
                    }
                    catch (Exception e)
                    {
                        messageErro = "ExecutaTrn: " + e.getMessage ();
                        System.err.println (messageErro);
                    }
                    processoI = null;

                    if (sitefTrOK) {
                        hndTela.sendEmptyMessage(0);
                    }

                }
            };
            processoI.start ();
        }
        else
        {
            hndTela.sendEmptyMessage (sts);
        }
    }

    // Aborta eventual processo interativo pendente anterior
    public void cancelaProcesso ()
    {
        if (processoI != null)
        {
            rodando = false;
            synchronized (processoI)
            {
                espera = false;
                processoI.notifyAll ();
            }
        }
    }

    private void RotinaColeta (int comando)
    {
        switch (comando)
        {
            case CliSiTefI.CMD_MENSAGEM_OPERADOR:
            case CliSiTefI.CMD_MENSAGEM_CLIENTE:
            case CliSiTefI.CMD_MENSAGEM:
                setStatusTransacao(clisitef.getBuffer ());
                break;
            case CliSiTefI.CMD_TITULO_MENU:
            case CliSiTefI.CMD_EXIBE_CABECALHO:
                mensagemVisor = clisitef.getBuffer ();
                break;
            case CliSiTefI.CMD_REMOVE_MENSAGEM_OPERADOR:
            case CliSiTefI.CMD_REMOVE_MENSAGEM_CLIENTE:
            case CliSiTefI.CMD_REMOVE_MENSAGEM:
            case CliSiTefI.CMD_REMOVE_TITULO_MENU:
            case CliSiTefI.CMD_REMOVE_CABECALHO:
                mensagemVisor = "";
                setStatusTransacao("");
                break;
            case 19:
            case CliSiTefI.CMD_CONFIRMA_CANCELA:
            {
                Intent i = new Intent (getApplicationContext (), SimNaoActivity.class);
                i.putExtra ("mensagemVisor", mensagemVisor);
                i.putExtra ("message", clisitef.getBuffer ());
                startActivityForResult (i, RequestCode.COLETA);
                return;
            }
            case CliSiTefI.CMD_OBTEM_CAMPO:
            case CliSiTefI.CMD_OBTEM_VALOR:
            {
                Intent i = new Intent (getApplicationContext (), DialogActivity.class);
                i.putExtra ("mensagemVisor", mensagemVisor);
                i.putExtra ("message", clisitef.getBuffer ());
                startActivityForResult (i, RequestCode.COLETA);
                return;
            }
            case CliSiTefI.CMD_SELECIONA_MENU:
            {
                Intent i = new Intent (getApplicationContext (), MenuActivity.class);
                i.putExtra ("mensagemVisor", mensagemVisor);
                i.putExtra ("message", clisitef.getBuffer ());
                startActivityForResult (i, RequestCode.COLETA);
                return;
            }
            case CliSiTefI.CMD_OBTEM_QUALQUER_TECLA:
            case CliSiTefI.CMD_PERGUNTA_SE_INTERROMPE:
                break;
            default:
                break;
        }
        synchronized (processoI)
        {
            espera = false;
            processoI.notifyAll ();
        }
    }

    private void RotinaResultado (int campo)
    {
        switch (campo)
        {
            case CAMPO_COMPROVANTE_CLIENTE:
                sitefTrOK = true;
                getIntent().putExtra("retCupomCliente", clisitef.getBuffer());
                break;
            case CAMPO_COMPROVANTE_ESTAB:
                sitefTrOK = true;
                getIntent().putExtra("retCupomEstab", clisitef.getBuffer());
                alert (clisitef.getBuffer ());
                break;
            default:
                break;
        }
        synchronized (processoI)
        {
            espera = false;
            processoI.notifyAll ();
        }
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RequestCode.COLETA)
        {
            if (resultCode == RESULT_OK)
            {
                String in = data.getExtras().getString("input");
                clisitef.setBuffer (in);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                clisitef.setContinuaNavegacao (-1);
            }
            synchronized (processoI)
            {
                espera = false;
                processoI.notifyAll ();
            }
        }
        else if (requestCode == RequestCode.VALOR)
        {
            if (resultCode == RESULT_OK)
            {
                double valorAux = data.getExtras().getDouble("valor", 0);
                executaTrn(modalidade, valorAux, sitefCupomFiscal, sitefData, sitefHora, sitefOperador, sitefRestricoes);
            }
            else if (requestCode == RESULT_CANCELED)
            {
                //finish ();
                setRetornoTela(false, "", true);
            }
        }
    }

    private void alert (String message)
    {
        Toast.makeText (XActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void setRetornoTela(boolean retVendaOK, String retVendaMsg, boolean fecharTela) {
        getIntent().putExtra("retVendaOK", retVendaOK);

        if ( !retVendaMsg.isEmpty() ){
            getIntent().putExtra("retVendaMsg", retVendaMsg);
        }

        if (fecharTela) {
            setResult(1, getIntent());
            finish();

        } else {
            //-- Não fecho a tela para que o usuário possa ver a mensagem e dar OK...
            if ( !retVendaMsg.isEmpty() ) {
                setStatusTransacao(retVendaMsg);
            }

            //-- Mudar o botão para OK...
            final Button btn = (Button) findViewById(ResourceId.getIdentifier("btCfgCancela", "id", package_name));

            btn.setVisibility(View.VISIBLE);
            btn.setText("OK");
        }
    }

    private void setStatusTransacao(String s)
    {
        statusTrn = s;
        String aux = statusCnx + "\n\r" + statusTrn;
        ((TextView) findViewById (ResourceId.getIdentifier("tvStatusTrn", "id", package_name))).setText (aux.trim());
        //((TextView) findViewById (R.id.tvStatusTrn)).setText (aux.trim());
    }

    private void setStatusConexao(String s)
    {
        statusCnx = s;
        String aux = statusCnx + "\n\r" + statusTrn;
        ((TextView) findViewById (ResourceId.getIdentifier("tvStatusTrn", "id", package_name))).setText (aux.trim());
        //((TextView) findViewById (R.id.tvStatusTrn)).setText (aux.trim());
    }

    private static Handler hndComando = new Handler ()
    {
        public void handleMessage (android.os.Message message)
        {
            //instance.setTitle (""); //-- Só para limpar eventuais mensagens de conexao do pinpad
            instance.setStatusConexao("");
            if (message.what == CliSiTefI.CMD_RETORNO_VALOR)
                instance.RotinaResultado (clisitef.getTipoCampo ());
            else
                instance.RotinaColeta(message.what);
        }
    };

    private static Handler hndMessage = new Handler ()
    {
        public void handleMessage (android.os.Message message)
        {
            switch (message.what)
            {
                case CliSiTefI.EVT_INICIA_ATIVACAO_BT:
                    //instance.setSupportProgressBarIndeterminateVisibility (true);
                    //instance.setTitle ("Ativando BT");
                    instance.setStatusConexao("Ativando BT");
                    break;
                case CliSiTefI.EVT_FIM_ATIVACAO_BT:
                    //instance.setProgressBarIndeterminateVisibility (false);
                    instance.setStatusConexao("PinPad");

                    break;
                case CliSiTefI.EVT_INICIA_AGUARDA_CONEXAO_PP:
                    //instance.setProgressBarIndeterminateVisibility (true);
                    instance.setStatusConexao("Aguardando pinpad");
                    break;
                case CliSiTefI.EVT_FIM_AGUARDA_CONEXAO_PP:
                    //instance.setProgressBarIndeterminateVisibility (false);
                    instance.setStatusConexao("");
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURANDO:
                    //instance.setProgressBarIndeterminateVisibility (true);
                    instance.setStatusConexao("Configurando pinpad");
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURADO:
                    //instance.setProgressBarIndeterminateVisibility (false);
                    instance.setStatusConexao("Pinpad configurado");
                    break;
                case CliSiTefI.EVT_PP_BT_DESCONECTADO:
                    //instance.setProgressBarIndeterminateVisibility (false);
                    instance.setStatusConexao("Pinpad desconectado");
                    break;
            }
        }
    };

    private static Handler hndTela = new Handler ()
    {
        public void handleMessage (android.os.Message message)
        {
            boolean lOK = (message.what==0);
            instance.setRetornoTela(lOK, instance.sitefErroDescr(message.what), lOK);
        }
    };

    private String sitefErroDescr(int erro) {
        String result = "";

        if (erro != 0 && erro != 10000 && erro > 0) {
            result = "Negado pelo autorizador.";

        } else {
            switch (erro) {
                case 0:
                    result = "Sucesso na execução da função.";
                    break;
                case 10000:
                    result = "Deve ser chamada a rotina de continuidade do processo.";
                    break;
                case -1:
                    result = "Módulo não inicializado. O PDV tentou chamar alguma rotina sem antes executara função configura.";
                    break;
                case -2:
                    result = "Operação cancelada pelo operador.";
                    break;
                case -3:
                    result = "O parâmetro função / modalidade é inválido.";
                    break;
                case -4:
                    result = "Falta de memória no PDV.";
                    break;
                case -5:
                    result = "Sem comunicação com o SiTef.";
                    break;
                case -6:
                    result = "Operação cancelada pelo usuário (no pinpad).";
                    break;
                case -7:
                    result = "Reservado";
                    break;
                case -8:
                    result = "A CliSiTef não possui a implementação da função necessária, provavelmente está desatualizada (a CliSiTefI é mais recente).";
                    break;
                case -9:
                    result = "A automação chamou a rotina ContinuaFuncaoSiTefInterativo sem antes iniciar uma função iterativa.";
                    break;
                case -10:
                    result = "Algum parâmetro obrigatório não foi passado pela automação comercial.";
                    break;
                case -12:
                    result = "Erro na execução da rotina iterativa. Provavelmente o processo iterativo anterior não foi finalizado até o final (enquanto o retorno for igual a 10000).";
                    break;
                case -15:
                    result = "Operação cancelada pela automação comercial.";
                    break;
                case -20:
                    result = "Parâmetro inválido passado para a função.";
                    break;
                case -21:
                    result = "Utilizada uma palavra proibida, por exemplo SENHA, para coletar dados em aberto no pinpad. Por exemplo na função ObtemDadoPinpadDiretoEx.";
                    break;
                case -25:
                    result = "Erro no Correspondente bancário: Deve realizar sangria.";
                    break;
                case -30:
                    result = "Erro de acesso ao arquivo. Certifique-se que o usuário que roda a aplicação tem direitos de leitura/escrita.";
                    break;
                case -40:
                    result = "Transação negada pelo SiTef.";
                    break;
                case -41:
                    result = "Dados inválidos.";
                    break;
                case -42:
                    result = "Retorno não previsto. Reservado";
                    break;
                case -43:
                    result = "Problema na execução de alguma das rotinas no pinpad.";
                    break;
                case -50:
                    result = "Transação não segura.";
                    break;
                case -100:
                    result = "Erro interno do módulo.";
                    break;
            }
        }
        return "["+Integer.toString(erro)+"] " + result;
    };

}