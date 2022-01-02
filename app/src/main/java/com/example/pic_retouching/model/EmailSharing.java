package com.example.pic_retouching.model;

import android.content.Context;
import android.content.SharedPreferences;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.util.Properties;

public class EmailSharing {

    private String str_from;
    private String str_to;
    private String str_content;
    private String str_subject;
    private Properties properties;
    private Session session;
    private InternetAddress[] to_address;
    private MimeMessage msg;
    private MimeMultipart multipart;
    private Bitmap bitmap;
    private InternetAddress address;

    public EmailSharing (String from, String to, String content, String subject, Bitmap bitmap) throws AddressException {
        properties = new Properties();
        str_from = from;
        str_content= content;
        str_to = to;
        str_subject = subject;
        this.bitmap = bitmap;
        address = new InternetAddress();
        init();
    }

    public void init() throws AddressException {
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", "smtp.163.com");
        properties.setProperty("mail.smtp.port", "25");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.dubug", "true");
        session = Session.getInstance(properties);
        msg = new MimeMessage(session);
        multipart = new MimeMultipart("mixed");
        address = new InternetAddress(str_from);
    }

    public boolean sendEmail() throws MessagingException {
        Log.e("email", "sendEmail: "+ address.getAddress());
        msg.setFrom(new InternetAddress(str_from));
        msg.setSubject(str_subject);
        msg.setText(str_content);
        msg.setSentDate(new Date(System.currentTimeMillis()));
        if(!str_to.isEmpty()) {
            String[] to_list = str_to.split(",");
            to_address = new InternetAddress[to_list.length];
            for (int i = 0; i < to_list.length; i++) {
                to_address[i] = new InternetAddress(to_list[i].trim());
            }
            addImage();
            msg.addRecipients(Message.RecipientType.TO, to_address);
        }
        // deal with "to"
        msg.saveChanges();
        Transport transport = session.getTransport();
        transport.connect(str_from, "LFRPWJQNEHRSNWSF");
        // password is the authorization code of "13453150629@163.com"
        transport.sendMessage(msg, msg.getAllRecipients());
        return true;
    }

    private void addImage() throws MessagingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageByte = outputStream.toByteArray();
        ByteArrayDataSource arrayDataSource = new ByteArrayDataSource(imageByte, "image/*");
        MimeBodyPart bodyPart = new MimeBodyPart();
        DataHandler dataHandler = new DataHandler(arrayDataSource);
        bodyPart.setDataHandler(dataHandler);
        bodyPart.setFileName("image");
        //bodyPart.setContent(arrayDataSource, "byteArray");
        multipart.addBodyPart(bodyPart);
        msg.setContent(multipart);
    }
}
