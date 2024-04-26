import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mybluetooth.R

class CameraFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var deleteButton: Button

    // Use registerForActivityResult to handle camera permissions and image capture
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                // Handle permission denial here
            }
        }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                imageView.setImageBitmap(imageBitmap)
                imageView.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        imageView = view.findViewById(R.id.selected_image_view)
        deleteButton = view.findViewById(R.id.button_delete_image)

        view.findViewById<Button>(R.id.button_add_image).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                openCamera()
            }
        }

        deleteButton.setOnClickListener {
            imageView.setImageBitmap(null)
            imageView.visibility = View.GONE
            deleteButton.visibility = View.GONE
        }

        return view
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(intent)
    }
}
