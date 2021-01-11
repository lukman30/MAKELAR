package com.baba.makelar.detail;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baba.makelar.adapter.AdapterFotoMobil;
import com.baba.makelar.adapter.AdapterInfoProduk;
import com.baba.makelar.adapter.AdapterInspeksi;
import com.baba.makelar.helper.Fungsi;
import com.baba.makelar.model.ModelInfoProduk;
import com.baba.makelar.model.ModelIsiKategori;
import com.baba.makelar.model.ModelKategori;
import com.baba.lajursurveyorsurveyor.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.hdodenhof.circleimageview.CircleImageView;

public class Detail extends AppCompatActivity {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Menu collapseMenu;
    private boolean appBarExpanded = true;
    int cek;
    Fungsi f;
    String idproduk;
    String[] judulnya = {
            "Data Ban","Dokumen","Fitur","Foto Kendaraan", "Tes Drive",
            "Eksternal Body","Eksternal Kaca Lampu","Eksternal Under Body","Internal Dashboard", "Internal Instrument",
            "Internal Jok Trim","Mesin Oli Cairan","Mesin Ruang Mesin","Tambahan Kelengkapan"
    };

    ImageView header;
    RecyclerView rvfoto, infoproduk, rvinspeksi;

    ArrayList<ModelInfoProduk> listInfoProduk = new ArrayList<>();
    ArrayList<ModelKategori> listnya = new ArrayList<>();
    ArrayList<String> listfotomobil = new ArrayList<>();
    ArrayList<String> listlengkapfotomobil = new ArrayList<>();

    AdapterInfoProduk aip;
    AdapterInspeksi ai;
    Button kirim;

    CardView cvkomponen, cvkodecepat, hasilperkiraanharga;
    CircleImageView fotopelanggan;
    TextView namapelanggan, alamat, hasilharga, phone, klikphone, statusharga;

    ImageView qrImage;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    TextView kode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        f = new Fungsi(this, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        f.setTransparantStatusBar();

        idproduk = getIntent().getExtras().getString("id");

        toolbar = findViewById(R.id.anim_toolbar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        appBarLayout = findViewById(R.id.appbar);
        cvkodecepat = findViewById(R.id.cvkodecepat);
        cvkomponen = findViewById(R.id.cvkomponen);
        hasilperkiraanharga = findViewById(R.id.hasilperkiraanharga);

        fotopelanggan =findViewById(R.id.fotopelanggan);
        namapelanggan = findViewById(R.id.namapelanggan);
        phone = findViewById(R.id.phone);
        klikphone = findViewById(R.id.klikphone);
        alamat = findViewById(R.id.alamat);
        statusharga = findViewById(R.id.statusharga);

        header = findViewById(R.id.header);
        hasilharga = findViewById(R.id.hasilharga);
        kirim = findViewById(R.id.kirim);
        kode=findViewById(R.id.kode);
        qrImage=findViewById(R.id.qr_image);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent data = getIntent();
        cek = data.getIntExtra("update", 0);

        if (cek==1){
            f.dr.child("daftarinspeksi2").child(data.getStringExtra("id")).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    System.out.println("CEKKKKKKKKKKKKKK");
                    System.out.println(dataSnapshot.child("status").getValue(String.class));


                    if (dataSnapshot.child("status").getValue(String.class).equals("Tiba")||dataSnapshot.child("status").getValue(String.class).equals("Sukses")){
                        cvkodecepat.setVisibility(View.GONE);

                    }else{

                        cvkodecepat.setVisibility(View.VISIBLE);
                        String idproduk=dataSnapshot.getKey();
                        String idpengguna=dataSnapshot.child("idpengguna").getValue(String.class);
                        String idsurveyor=dataSnapshot.child("surveyor").getValue(String.class);

                        kode.setText(idproduk+"|"+idpengguna+"|"+idsurveyor);
                        cetak();
                    }





                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }



        collapsingToolbar.setTitle(data.getStringExtra("merk"));
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lajur);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {

            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {
                int vibrantColor = palette.getVibrantColor(R.color.colorPrimary);
                collapsingToolbar.setContentScrimColor(vibrantColor);
                collapsingToolbar.setStatusBarScrimColor(R.color.colorPrimary);
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(Math.abs(verticalOffset) > 200){
                    appBarExpanded = false;
                }else{
                    appBarExpanded = true;
                }
                invalidateOptionsMenu();
            }
        });

        ambilFoto();
        infoProduk();
        f.dr.child("daftarinspeksi2").child(idproduk).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue().equals("Tiba")||dataSnapshot.getValue().equals("Sukses")){
                    komponenInspeksi();
                    f.dr.child("daftarinspeksi2").child(idproduk).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot hi) {
                            if(hi.child("harga").exists()){
                                if(!hi.child("harga").getValue(String.class).equals("kosong")){
                                    cvkomponen.setVisibility(View.GONE);
                                    kirim.setVisibility(View.GONE);
                                    hasilperkiraanharga.setVisibility(View.VISIBLE);
//                                    hasilharga.setText(hi.child("harga").getValue(String.class));
                                    hasilharga.setText("Anda Sudah Bertemu Klien");
                                }else{
                                    cvkomponen.setVisibility(View.VISIBLE);
                                    kirim.setVisibility(View.VISIBLE);
                                    hasilperkiraanharga.setVisibility(View.GONE);
                                    hasilharga.setText("...");
                                }
                                statusharga.setText("Status : "+hi.child("status").getValue(String.class));
                            }else{
                                cvkomponen.setVisibility(View.VISIBLE);
                                kirim.setVisibility(View.VISIBLE);
                                hasilperkiraanharga.setVisibility(View.GONE);
                                hasilharga.setText("...");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    cvkodecepat.setVisibility(View.VISIBLE);
                    cvkomponen.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ambilFoto() {
        rvfoto = findViewById(R.id.rvfoto);
        rvfoto.setLayoutManager(new GridLayoutManager(this, 5));
        rvfoto.setHasFixedSize(true);
        final AdapterFotoMobil afm = new AdapterFotoMobil(this, this, listfotomobil);
        rvfoto.setAdapter(afm);

        f.dr.child("FotoProduk").child(idproduk).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listfotomobil.clear();
                listlengkapfotomobil.clear();
                int index = 0;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(index < 5){
                        listfotomobil.add(ds.getValue(String.class));
                    }
                    listlengkapfotomobil.add(ds.getValue(String.class));
                    index++;
                }
                afm.sisafoto(((int)dataSnapshot.getChildrenCount() - listfotomobil.size()));
                afm.datalengkap(listlengkapfotomobil);
                afm.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void komponenInspeksi() {
//        cvkodecepat.setVisibility(View.GONE);
//        cvkomponen.setVisibility(View.VISIBLE);
        rvinspeksi = findViewById(R.id.rvinspeksi);
        rvinspeksi.setLayoutManager(new GridLayoutManager(this, 1));
        rvinspeksi.setHasFixedSize(true);
        ai = new AdapterInspeksi(this, this, listnya, idproduk);
        rvinspeksi.setAdapter(ai);
        f.dr.child("master").child("kategori").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listnya.clear();
                int index = 0;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ArrayList<ModelIsiKategori> listisikategori = new ArrayList<>();
                    ModelKategori m = new ModelKategori();
                    m.setId(ds.getKey());
                    m.setJudul(judulnya[index]);
                    m.setTerbuka(false);
                    for(DataSnapshot ds2: ds.getChildren()){
                        ModelIsiKategori mik = new ModelIsiKategori();
                        mik.setId(ds2.getKey());
                        mik.setJudul(ds2.getValue(String.class));
                        listisikategori.add(mik);
                        m.setListisikategori(listisikategori);
                    }
                    listnya.add(m);
                    index++;
                }
                ai.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void infoProduk() {
        infoproduk = findViewById(R.id.infoproduk);
        infoproduk.setLayoutManager(new GridLayoutManager(this, 2));
        infoproduk.setHasFixedSize(true);
        aip = new AdapterInfoProduk(this, this, listInfoProduk);
        infoproduk.setAdapter(aip);

        f.dr.child("produkMakelar").child(idproduk).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listInfoProduk.clear();
                Picasso.with(Detail.this).load(dataSnapshot.child("foto").getValue(String.class)).into(header);
                ModelInfoProduk merk = new ModelInfoProduk();
                String d_idpengguna = "";
                String d_merk = "";
                String d_model = "";
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelInfoProduk m = new ModelInfoProduk();
                    String keynya = ds.getKey();

                    if(keynya.equals("idpengguna")){
                        d_idpengguna = ds.getValue(String.class);
                        f.dr.child("user").child(ds.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot user) {
                                Picasso.with(Detail.this).load(user.child("foto").getValue(String.class)).into(fotopelanggan);
                                namapelanggan.setText(user.child("namalengkap").getValue(String.class));
                                phone.setText(user.child("nohp").getValue(String.class)+" -");
                                klikphone.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        f.panggilTelepon(user.child("nohp").getValue(String.class));
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    if(keynya.equals("alamat")){
                        alamat.setText("Alamat : "+ds.getValue(String.class));
                    }

                    if(!keynya.equals("alamat") && !keynya.equals("foto") && !keynya.equals("idnya") && !keynya.equals("idpengguna") && !keynya.equals("lokasi") && !keynya.equals("provinsi") && !keynya.equals("status")){
                        m.setIkon(R.mipmap.ic_launcher_round);
                        m.setKey(keynya);
                        if(!keynya.equals("tahunmobil")){
                            m.setKomponen(ds.getValue(String.class));
                        }else{
                            m.setKomponen(String.valueOf(ds.getValue(Long.class)));
                        }
                        listInfoProduk.add(m);
                        if(keynya.equals("merk")){
                            merk = m;
                            d_merk = ds.getValue(String.class);
                        }else if(keynya.equals("model")){
                            d_model = ds.getValue(String.class);
                        }
                    }
                }
                int itemPos = listInfoProduk.indexOf(merk);
                listInfoProduk.remove(itemPos);
                listInfoProduk.add(0, merk);
                aip.notifyDataSetChanged();

                final String finalD_idpengguna = d_idpengguna;
                final String finalD_merk = d_merk;
                final String finalD_model = d_model;

                kirim.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        f.dr.child("master").child("kategori").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot daftarkategori) {
                                f.dr.child("tempInspeksi").child(idproduk).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot tempInspeksi) {
                                        Log.e("lajursurve", daftarkategori.getChildrenCount()+" / "+tempInspeksi.getChildrenCount());
                                        if(daftarkategori.getChildrenCount() != tempInspeksi.getChildrenCount()){
                                            AlertDialog.Builder ab = new AlertDialog.Builder(Detail.this);
                                            ab.setCancelable(false);
                                            ab.setMessage("Anda yakin akan mengirim data komponen mobil sekarang?");
                                            ab.setPositiveButton("Yakin", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.cancel();
                                                    HashMap<String, Object> map = new HashMap<>();
                                                    map.put("/hasilInspeksi/"+idproduk+"/harga", "kosong");
                                                    map.put("/hasilInspeksi/"+idproduk+"/harga_a", "kosong");
                                                    map.put("/hasilInspeksi/"+idproduk+"/harga_b", "kosong");
                                                    map.put("/hasilInspeksi/"+idproduk+"/idpengguna", finalD_idpengguna);
                                                    map.put("/hasilInspeksi/"+idproduk+"/merk", finalD_merk);
                                                    map.put("/hasilInspeksi/"+idproduk+"/model", finalD_model);
                                                    map.put("/hasilInspeksi/"+idproduk+"/status", "Menunggu");
                                                    map.put("/hasilInspeksi/"+idproduk+"/zhasil", tempInspeksi.getValue());
                                                    f.dr.updateChildren(map, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if(databaseError != null){

                                                            }else{
                                                                Toast.makeText(Detail.this, "Data komponen berhasil dikirim. Silahkan Tunggu..!!!", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                            ab.setNegativeButton("Batal", null);
                                            ab.show();
                                        }else{
                                            Toast.makeText(Detail.this, "Silahkan melengkapi semua data komponen mobil yang sedang Anda inspeksi terlebih dahulu..!!!", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    void cetak (){
        if (kode.getText().toString().length() > 0) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;
            qrgEncoder = new QRGEncoder(
                    kode.getText().toString(), null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.v("LOG", e.toString());
            }
        } else {
            Toast.makeText(getApplicationContext(),"Kode Kosong",Toast.LENGTH_LONG).show();
        }
    }

}
