package ar.edu.unpa.uarg.metricas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.RatingBar;

public class DialogoEstrellas extends AppCompatDialogFragment {
    private DialogoEstrellasListener listener;
    private RatingBar ratingBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View starDialogView = getActivity().getLayoutInflater().inflate(R.layout.layout_dialogo_estrellas, null);

        builder.setTitle("¿Qué puntaje le daría a esta aplicación?");

        builder.setPositiveButton("Calificar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Float score = ratingBar.getRating();

                listener.saveScore(score);
            }
        });

        builder.setView(starDialogView);

        ratingBar = starDialogView.findViewById(R.id.estrellas_puntaje);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DialogoEstrellasListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public interface DialogoEstrellasListener {
        void saveScore(Float score);
    }

}
