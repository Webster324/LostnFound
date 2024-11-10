package com.joe.assignment2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenerateQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenerateQRFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button printButton,howToUseButton;
    private ImageView QRImage;
    private Bitmap qr_code;
    private SharedPreferences sharedPreferences;
    private String userId;



    public GenerateQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenerateQRFragment newInstance(String param1, String param2) {
        GenerateQRFragment fragment = new GenerateQRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generate_qr, container, false);

        QRImage =  view.findViewById(R.id.QRImageView);
        printButton = view.findViewById(R.id.printButton);
        howToUseButton =  view.findViewById(R.id.howToUseButton);
        sharedPreferences = getActivity().getSharedPreferences("user info", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user id", "");

        // The url that the QR code will contain
        String url = "https://17hieng.com/lostnfound?userId=" + userId;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            // Generate QR code that contain url data and attach it to QRImage
            BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qr_code = barcodeEncoder.createBitmap(bitMatrix);
            QRImage.setImageBitmap(qr_code);
        }catch (WriterException e){
            throw new RuntimeException(e);
        }

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printQRCode();
            }
        });
        howToUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment instructionFragment = new InstructionFragment();
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment previousFragment = fragmentManager.findFragmentById(R.id.qr_fragment_container);
                if (previousFragment != null) {
                    fragmentTransaction.remove(previousFragment);
                }
                fragmentTransaction.replace(R.id.qr_fragment_container, instructionFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    private void printQRCode() {
        // Get the system's print manager service
        PrintManager printManager = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);
        String jobName = "PrintQRCode";
        if (printManager != null) {
            // Start the printing process with a print document adapter
            printManager.print(jobName, new PrintDocumentAdapter() {
                @Override
                public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                    // Respond to cancellation request
                    if (cancellationSignal.isCanceled()) {
                        callback.onLayoutCancelled();
                        return;
                    }
                    // Return print information to print framework
                    PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
                    callback.onLayoutFinished(info, true);
                }

                @Override
                public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                    /// Respond to writing cancellation request
                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        return;
                    }
                    try {
                        // Create a new PDF document
                        PdfDocument document = new PdfDocument();
                        // Define the page information(width,height,number) for the PDF document
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                        // Start a new page for the PDF document
                        PdfDocument.Page page = document.startPage(pageInfo);
                        // Create canvas for drawing in PDF document
                        Canvas canvas = page.getCanvas();

                        for (int i = 0; i < 20; i++) {
                            // Resize the QR code
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(qr_code, 100, 100, false);
                            // Adjust x position to create four QR codes in a row
                            int x = 30 + i % 4 * 140;
                            // Adjust y position to go down to the next row after four QR codes
                            int y = 30 + i / 4 * 140;
                            canvas.drawBitmap(resizedBitmap, x, y, null);

                            // Draw a border for the QR code
                            Paint rectPaint = new Paint();
                            rectPaint.setStyle(Paint.Style.STROKE);
                            rectPaint.setStrokeWidth(2);
                            canvas.drawRect(x - 5, y - 5, x + 115, y + 125, rectPaint);

                            // Draw a text under the QR code
                            Paint textPaint = new Paint();
                            textPaint.setTextSize(16);
                            canvas.drawText("Scan Me", x + 15, y + 110, textPaint);
                        }
                        // Finish the page for the PDF document
                        document.finishPage(page);
                        // Write the PDF document to the output stream
                        document.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                        document.close();

                        // Inform the print framework the document is complete
                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } catch (IOException e) {
                        // Inform the print framework the writing is failed
                        callback.onWriteFailed(e.getMessage());
                    }
                }
            }, null);
        } else {
            // Display a failure Message if the printing is not available
            Toast.makeText(getContext(), "Could not print QR code", Toast.LENGTH_SHORT).show();
        }
    }



}