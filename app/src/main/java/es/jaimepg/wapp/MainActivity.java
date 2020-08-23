package es.jaimepg.wapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
public class MainActivity extends AppCompatActivity {



    EditText numeroDeTelefono;
    EditText direccionIP;
    EditText numeroDePuerto;
    private TextView textoPetition;




    //Constantes para identificar intents
    private static int ACTIVIDAD2_ID = 13;

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mostrar la ventana para esta actividad
        setContentView(R.layout.activity_main); //definida en content_main.xml

        Log.d("clienteWApp", "fin onCreate()");
    } //()



    //-------------------------------------------------------------------------
    //---------  AL PULSAR EL BOTÓN "ENTRAR"  ---------------------------------
    //-------------------------------------------------------------------------
    public void boton_entrar_pulsado (View quien) {
        Log.d("clienteWApp", "Botón Entrar pulsado");

        numeroDeTelefono = (EditText) findViewById(R.id.editPhone);
        direccionIP = (EditText) findViewById(R.id.editIP);
        numeroDePuerto = (EditText) findViewById(R.id.editPuerto);

        final int numeroDeTelefonoString = Integer.valueOf(numeroDeTelefono.getText().toString());
        final String direccionIPString = String.valueOf(direccionIP.getText().toString());
        final int numeroDePuertoString = Integer.valueOf(numeroDePuerto.getText().toString());

        //si el teléfono no tiene 9 dígitos, mostrar alerta con mensaje de error
        Log.d("clienteWApp", "Dígitos teléfono = " + numeroDeTelefono.length());
        if(numeroDeTelefono.length() != 9){
            abrirAlertDialog("ERROR: El teléfono introducido no es válido. Un teléfono debe tener 9 dígitos.");

        //si el teléfono es válido, se busca en la base de datos si existe el número
        } else {
            Log.d("clienteWApp", "Teléfono válido, buscando si existe...");

            //PETICIÓN REST BuscarUsario() "GET http://IP:puerto/usuario/numeroDeTelefono"
            final PeticionarioREST elPeticionario = new PeticionarioREST();
            Log.d("clienteWApp", "Orden SQL = GET http://" + direccionIPString + ":" + numeroDePuertoString + "/usuario/" + numeroDeTelefonoString);
            elPeticionario.hacerPeticionREST("GET", "http://" + direccionIPString + ":" + numeroDePuertoString + "/usuario/" + numeroDeTelefonoString, null,
                    new PeticionarioREST.RespuestaREST() {
                        @Override
                        public void callback(int codigo, String cuerpo) {
                            Log.d("clienteWApp", "RESPUESTA CÓDIGO = " + codigo + ";;; CUERPO =" + cuerpo);

                            if (codigo == 0){
                                //Si hay problemas de conexión
                                abrirAlertDialog("Error de conexión con el servidor. Pruebe con otra dirección IP o inténtelo más tarde.");
                            }

                            else if (codigo == 200) {
                                //El usuario se encuentra en la base de datos
                                Log.d("clienteWApp", "200 OK: El usuario existe");
                                Toast.makeText(getApplicationContext(), "Sesión iniciada para el " + numeroDeTelefonoString, Toast.LENGTH_SHORT).show();

                                //Se inicia la siguiente actividad
                                Intent intencion = new Intent(MainActivity.this, UserActivity.class);
                                intencion.putExtra("cuerpo", cuerpo);
                                intencion.putExtra("numeroDeTelefonoString", numeroDeTelefonoString);
                                intencion.putExtra("direccionIPString", direccionIPString);
                                intencion.putExtra("numeroDePuertoString", numeroDePuertoString);
                                startActivityForResult(intencion, ACTIVIDAD2_ID);

                            } else if (codigo == 404) {
                                //Si el usuario no está en la base de datos
                                Log.d("clienteWApp", "404 NOT FOUND: No se encuentra el usuario en la base de datos");
                                abrirAlertDialog("Este usuario no está registrado.");
                            }
                        }
                    }
            );
        }
    } //()




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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            //--------------------------------------------------------------------------------------
            // BOTÓN DE PETICIÓN REST DE PRUEBA ----------------------------------------------------
            //--------------------------------------------------------------------------------------
            case R.id.action_petitionrequest:
                // la acción se escribe aquí
                Log.d("clienteWApp", "Botón PetitionRequest pulsado");

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle("Respuesta REST");
                dialog.setContentView(R.layout.petition_dialog);
                dialog.show();

                this.textoPetition = (TextView) dialog.findViewById(R.id.textpetition);

                //HACIENDO LA PETICIÓN REST
                PeticionarioREST elPeticionarioPrueba = new PeticionarioREST();
                elPeticionarioPrueba.hacerPeticionREST("GET",  "https://httpbin.org/get", null,
                     /*   elPeticionario.hacerPeticionREST("PUT", "http://192.168.1.113:8080/persona",
                        "{\"dni\": \"A9182342W\", \"nombre\": \"Android\", \"apellidos\": \"De Los Palotes\"}",   */
                        new PeticionarioREST.RespuestaREST () {
                            @Override
                            public void callback(int codigo, String cuerpo) {
                                textoPetition.setText ("RESPUESTA = " + codigo + " <-> \n" + cuerpo);
                            }
                        }
                );

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
            // BOTÓN DE PASAR DIRECTO A LA SIGUIENTE ACTIVIDAD -------------------------------------
            //--------------------------------------------------------------------------------------
            case R.id.action_next:
                // la acción se escribe aquí
                Log.d("clienteWApp", "Botón PetitionResquest pulsado");

                //Se inicia la siguiente actividad
                Intent intencion = new Intent(MainActivity.this, UserActivity.class);
                intencion.putExtra("cuerpo", "Sin respuest REST");
                intencion.putExtra("numeroDeTelefonoString", 0);
                intencion.putExtra("direccionIPString", 0);
                intencion.putExtra("numeroDePuertoString", 0);
                startActivityForResult(intencion, ACTIVIDAD2_ID);

            //--------------------------------------------------------------------------------------
            default:
                return super.onOptionsItemSelected(item);
        }
    } //()



} // class