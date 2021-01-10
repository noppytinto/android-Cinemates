package mirror42.dev.cinemates.utilities;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class CodeSnippets {



    private void Toasts() {
        Context contextToSupply = null;

        // non-centered
        Toast toast = Toast.makeText(contextToSupply, "message", Toast.LENGTH_SHORT);
        toast.show();

        // centered
        Toast toastCentered = Toast.makeText(contextToSupply, "messagee", Toast.LENGTH_SHORT);
        toastCentered.setGravity(Gravity.CENTER, 0, 0);
        toastCentered.show();
    }

}
