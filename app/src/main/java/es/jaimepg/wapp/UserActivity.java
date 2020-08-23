package es.jaimepg.wapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class UserActivity extends AppCompatActivity {





    private TextView textoPetition;


    //Constantes para identificar intents
    private static int ACTIVIDAD2_ID = 13;

    TextView TextLoggedPhone;
    TextView TextEstado;

    String cuerpoForDialog;
    String cuerpoMensajesRecuperados;
    int codigoMensajesRecuperados;

    String direccionIP;
    int numeroDePuerto;
    int numeroDeTelefono;

    EditText telefonoAlQueEnviar;
    EditText textoQueEnviar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.d("clienteWApp", "INICIO DE USER_ACTIVITY");

        TextLoggedPhone = (TextView) findViewById(R.id.textViewLoggedPhone);
        TextEstado = (TextView) findViewById(R.id.textViewEstado);

        //Recuperando los parámetros de MainActivity
        String cuerpo = getIntent().getExtras().getString("cuerpo");
        int numeroDeTelefonorecuperado = getIntent().getExtras().getInt("numeroDeTelefonoString");
        String direccionIPrecuperada = getIntent().getExtras().getString("direccionIPString");
        int numeroDePuertorecuperado = getIntent().getExtras().getInt("numeroDePuertoString");

        direccionIP = direccionIPrecuperada;
        numeroDePuerto = numeroDePuertorecuperado;
        numeroDeTelefono = numeroDeTelefonorecuperado;

        //Mostrando el número de teléfono que ha iniciado sesión
        TextLoggedPhone.setText(String.valueOf(numeroDeTelefono));

        //Se copia el cuerpo para poder mostrarlo en el dialog si se desea
        cuerpoForDialog = cuerpo;


        //JSON Parsing -> BuscandoUsuario()
        Log.d("clienteWApp", "Se parsea el estado del usuario");
        try{
            JSONObject jsonBuscandoUsuario=(new JSONObject(cuerpo));
            String jsonEstado=jsonBuscandoUsuario.getString("estado");
            //Mostrando el estado del usuario
            TextEstado.setText("" +jsonEstado);

        }catch (Exception e) {e.printStackTrace();}

   }//()




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentRespuesta){


        if(requestCode == ACTIVIDAD2_ID) {
            if (resultCode == RESULT_OK) {
            }
        }
    }



    //-------------------------------------------------------------------------
    //---------  AL PULSAR EL BOTÓN "ENTRAR"  ---------------------------------
    //-------------------------------------------------------------------------
    public void boton_recibir_pulsado (View quien) {

        final ListView list = (ListView) findViewById(android.R.id.list);

        //HACIENDO LA PETICIÓN REST
        PeticionarioREST elPeticionario = new PeticionarioREST();
        Log.d("clienteWApp", "Orden SQL = GET http://" + direccionIP +":" + numeroDePuerto + "/mensaje/" + numeroDeTelefono);
        elPeticionario.hacerPeticionREST("GET", "http://" + direccionIP +":" + numeroDePuerto + "/mensaje/" + numeroDeTelefono, null,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo3, String cuerpo3) {
                        Log.d("clienteWApp", "RESPUESTA CÓDIGO = " + codigo3 + ";;; CUERPO =" + cuerpo3);
                        cuerpoMensajesRecuperados = cuerpo3;
                        codigoMensajesRecuperados = codigo3;

                        // Hashmap for ListView
                        ArrayList<HashMap<String, String>> messagesList = new ArrayList<HashMap<String, String>>();

                        ListAdapter adapter = new SimpleAdapter(UserActivity.this, messagesList, R.layout.message_main,
                                new String[]{"tel_origen", "mensaje"},
                                new int[]{R.id.originPhone, R.id.textReceived});

                        list.setAdapter(adapter);


                        //JSON Parsing -> RecuperarMensajesPara()
                        if (codigoMensajesRecuperados == 200) {

                            Log.e("WApp", "Se están buscando mensajes");
                            try {
                                //JSONObject jsonRMP = (new JSONObject(cuerpoMensajesRecuperados));
                                JSONArray jsonRMP = (new JSONArray(cuerpoMensajesRecuperados));

                                //JSONArray telefonosOrigen = jsonRMP.getJSONArray("");

                                // looping through All
                                for (int i = 0; i < jsonRMP.length(); i++) {
                                    JSONObject c = jsonRMP.getJSONObject(i);

                                    String tel = c.getString("tel_origen");
                                    String mens = c.getString("mensaje");

                                    // tmp hashmap for single student
                                    HashMap<String, String> jsonRMPsingle = new HashMap<String, String>();

                                    // adding every child node to HashMap key => value
                                    jsonRMPsingle.put("tel_origen", tel);
                                    jsonRMPsingle.put("mensaje", mens);

                                    // adding student to students list
                                    messagesList.add(jsonRMPsingle);
                                }

                                Log.e("WApp", "jsonRMP.length = " +jsonRMP.length());

                                if (jsonRMP.length() == 0) {
                                    Log.e("WApp", "No hay mensajes nuevos");
                                    Toast.makeText(getApplicationContext(), "No hay mensajes nuevos", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }//()




    //-------------------------------------------------------------------------
    //---------  AL PULSAR EL BOTÓN "ENTRAR"  ---------------------------------
    //-------------------------------------------------------------------------
    public void boton_escribirmensaje_pulsado (View quien) {


        Log.d("clienteWApp", "Botón EscribirMensaje pulsado");

        final Dialog dialog = new Dialog(UserActivity.this);
        dialog.setContentView(R.layout.newmessage_dialog);
        dialog.show();

        //Botón de CANCELAR mensaje
        final Button cancelButton = (Button)dialog.findViewById(R.id.buttonCancelarMensaje);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.cancel();
            }
        });


        //Botón de ENVIAR mensaje
        final Button sendButton = (Button)dialog.findViewById(R.id.buttonEnviarMensaje);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("clienteWApp", "Botón EnviarMensaje pulsado");

                telefonoAlQueEnviar = (EditText) dialog.findViewById(R.id.editTelefonoDestino);
                textoQueEnviar = (EditText) dialog.findViewById(R.id.editTextMensajeText);

                final int telefonoAlQueEnviarString = Integer.valueOf(telefonoAlQueEnviar.getText().toString());
                final String textoQueEnviarString = String.valueOf(textoQueEnviar.getText().toString());

                String bodyQueEnviar = "{\"to\": \"" + numeroDeTelefono + "\", \"td\": \"" + telefonoAlQueEnviarString + "\", \"msj\": \"" + textoQueEnviarString + "\"}";


                //PETICIÓN REST BuscarUsario() "PUT http://IP:puerto/mensaje"
                final PeticionarioREST elPeticionario = new PeticionarioREST();
                Log.d("clienteWApp", "Orden SQL = PUT http://" + direccionIP +":" + numeroDePuerto + "/mensaje");
                Log.d("clienteWApp", "Body SQL = " +bodyQueEnviar);
                elPeticionario.hacerPeticionREST("PUT", "http://" + direccionIP +":" + numeroDePuerto + "/mensaje", bodyQueEnviar,
                        new PeticionarioREST.RespuestaREST() {
                            @Override
                            public void callback(int codigo, String cuerpo) {
                                Log.d("clienteWApp", "RESPUESTA CÓDIGO = " + codigo + ";;; CUERPO =" + cuerpo);

                                if (codigo == 0){
                                    Log.d("clienteWApp", "Sucede: error de conexión");
                                    //Toast.makeText(getApplicationContext(), "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                    abrirAlertDialog("Error de conexión");
                                }

                                else if (codigo == 200) {
                                    Log.d("clienteWApp", "Todo OK: Mensaje enviado");
                                    Toast.makeText(getApplicationContext(), "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                    dialog.cancel();

                                } else if (codigo == 500 || codigo == 409) {
                                    Log.d("clienteWApp", "Sucede: error interno del servidor");
                                   // Toast.makeText(getApplicationContext(), "Error interno del servidor", Toast.LENGTH_SHORT).show();

                                    abrirAlertDialog("Error interno del servidor");


                                } else {
                                    dialog.cancel();
                                }
                            }
                        });

            }
        });





    }//()



    //-------------------------------------------------------------------------
    //---------  MÉTODO PARA ABRIR "ALTER DIALOGS" CON MENSAJE ----------------
    //-------------------------------------------------------------------------
    public void abrirAlertDialog (String texto){

        new AlertDialog.Builder(this)
                //.setTitle("Error")
                .setMessage("" +texto)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Aceptar, se cierra la alerta
                    }
                })
                .show();

    } //()








        //-------------------------------------------------------------------------
    //---------  MENÚ DE OPCIONES  --------------------------------------------
    //-------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            //--------------------------------------------------------------------------------------
            // BOTÓN DE MUESTRA DE CUERPO REST BUSCARUSUARIO() -------------------------------------
            //--------------------------------------------------------------------------------------
            case R.id.action_restBuscarUsuario:
                // la acción se escribe aquí
                Log.d("clienteWApp", "Botón REST BuscarUsuario pulsado");

                final Dialog dialog = new Dialog(UserActivity.this);
                dialog.setTitle("Respuesta REST");
                dialog.setContentView(R.layout.petition_dialog);
                dialog.show();

                this.textoPetition = (TextView) dialog.findViewById(R.id.textpetition);
                textoPetition.setText(cuerpoForDialog);

                //Botón de CERRAR el diálogo
                final Button closeButton = (Button)dialog.findViewById(R.id.button_cerrardialog);
                closeButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.cancel();
                    }
                });


                return true;

            //--------------------------------------------------------------------------------------
            // BOTÓN DE MUESTRA DE RECUPERARMENSAJESPARA()  ----------------------------------------
            //--------------------------------------------------------------------------------------
            case R.id.action_restRecuperarMensajesPara:
                // la acción se escribe aquí
                Log.d("clienteWApp", "Botón RecuperarMensajesPara pulsado");

                final Dialog dialog2 = new Dialog(UserActivity.this);
                dialog2.setTitle("Respuesta REST");
                dialog2.setContentView(R.layout.petition_dialog);
                dialog2.show();

                this.textoPetition = (TextView) dialog2.findViewById(R.id.textpetition);

                final int TextLoggedPhoneString = Integer.valueOf(TextLoggedPhone.getText().toString());


                //HACIENDO LA PETICIÓN REST
                PeticionarioREST elPeticionario2 = new PeticionarioREST();
                Log.d("clienteWApp", "Orden SQL = GET http://192.168.1.200:8085/mensaje/" + TextLoggedPhoneString);
                elPeticionario2.hacerPeticionREST("GET", "http://192.168.1.200:8085/mensaje/" + TextLoggedPhoneString, null,
                        new PeticionarioREST.RespuestaREST() {
                            @Override
                            public void callback(int codigo2, String cuerpo2) {
                                Log.d("clienteWApp", "RESPUESTA CÓDIGO = " + codigo2 + ";;; CUERPO =" + cuerpo2);

                                textoPetition.setText(cuerpo2);
                }
                        });




                //Botón de CERRAR el diálogo
                final Button closeButton2 = (Button)dialog2.findViewById(R.id.button_cerrardialog);
                closeButton2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog2.cancel();
                    }
                });

                return true;


            //--------------------------------------------------------------------------------------
            // BOTÓN DE LOAD (RECIBIR MENSAJES NUEVOS) ---------------------------------------------
            //--------------------------------------------------------------------------------------
            case R.id.action_load:
                // la acción se escribe aquí
                Log.d("clienteWApp", "Botón Load pulsado");

                Button botonRecargar = (Button)findViewById(R.id.buttonRecibir);
                botonRecargar.performClick();

                return true;
            //--------------------------------------------------------------------------------------


            //--------------------------------------------------------------------------------------
            // BOTÓN DE WRITE (ESCRIBIR NUEVO MENSAJE) ---------------------------------------------
            //--------------------------------------------------------------------------------------
            case R.id.action_write:
                // la acción se escribe aquí
                Log.d("clienteWApp", "Botón Load pulsado");

                Button botonWriteNuevo = (Button)findViewById(R.id.buttonEscribirMensaje);
                botonWriteNuevo.performClick();

                return true;
            //--------------------------------------------------------------------------------------



            default:
                return super.onOptionsItemSelected(item);
        }
    } //()





}
