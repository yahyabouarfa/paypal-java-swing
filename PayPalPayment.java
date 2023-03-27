import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalPayment {
    
    private static final String CLIENT_ID = "Aerc9QIrYT0bq4YjPyGiMtUnW9wpuJsk5snIkMe-1etnN93GL6j0p_bsH6JNMHD430TCuEZA-cC3x-8q";
    private static final String CLIENT_SECRET = "EGr4_JiHlLb-c2WsPolOfmqAce2AjYBxKDI5S9O-o6VIGuE5FpCUZ6QSpYL8u8ENHNsGZgsXkqb9vy4s";
    private static final String MODE = "sandbox";
    private static final String CURRENCY = "USD";
    private static final double AMOUNT_VALUE = 10.0;
    private static final String ITEM_NAME = "Item";
    private static final String ITEM_DESCRIPTION = "Item Description";
    
    private static APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
    
    private static JFrame frame;
    private static JPanel panel;
    private static JLabel amountLabel;
    private static JTextField amountField;
    private static JButton payButton;
    
    public static void main(String[] args) {
        createUI();
    }
    
    private static void createUI() {
        frame = new JFrame("Pay with PayPal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new JPanel();
        panel.setLayout(null);
        
        amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(10, 10, 80, 25);
        panel.add(amountLabel);
        
        amountField = new JTextField(20);
        amountField.setBounds(100, 10, 160, 25);
        panel.add(amountField);
        
        payButton = new JButton("Pay with PayPal");
        payButton.setBounds(10, 40, 250, 25);
        payButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                double amountValue = AMOUNT_VALUE;
                try {
                    amountValue = Double.parseDouble(amountField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid amount value.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Payment payment = createPayment(amountValue);
                    String approvalUrl = getApprovalUrl(payment);
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(approvalUrl));
                } catch (PayPalRESTException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(payButton);
        
        frame.getContentPane().add(panel);
        frame.setSize(300, 120);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static Payment createPayment(double amountValue) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setTotal(String.format("%.2f", amountValue));
        
        Item item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setQuantity("1");
        item.setPrice(amount.getTotal());
        
        ItemList itemList = new ItemList();
        itemList.setItems(new Item[] {item});
        Details details = new Details();
        details.setShipping("0");
        details.setTax("0");
        details.setSubtotal(amount.getTotal());
        
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setItemList(itemList);
        transaction.setDescription("Transaction Description");
        transaction.setInvoiceNumber(String.valueOf(System.currentTimeMillis()));
        transaction.setCustom("Custom Value");
        transaction.setSoftDescriptor("Soft Descriptor");
        transaction.setDetails(details);
        
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:8080/cancel");
        redirectUrls.setReturnUrl("http://localhost:8080/success");
        
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(getPayer());
        payment.setTransactions(new Transaction[] {transaction});
        payment.setRedirectUrls(redirectUrls);
        
        return payment.create(apiContext);
    }
    
    private static Payer getPayer() {
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        
        return payer;
    }
    
    private static String getApprovalUrl(Payment payment) throws PayPalRESTException {
        for (Links link : payment.getLinks()) {
            if (link.getRel().equals("approval_url")) {
                return link.getHref();
            }
        }
        
        throw new PayPalRESTException("No approval_url found in payment.");
    }
}