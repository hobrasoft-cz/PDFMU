/*
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.hobrasoft.pdfmu.operation;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateInfo.X500Name;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import cz.hobrasoft.pdfmu.MapSorter;
import cz.hobrasoft.pdfmu.PreferenceListComparator;
import cz.hobrasoft.pdfmu.jackson.CertificateResult;
import cz.hobrasoft.pdfmu.jackson.Inspect;
import cz.hobrasoft.pdfmu.jackson.Signature;
import cz.hobrasoft.pdfmu.jackson.SignatureDisplay;
import cz.hobrasoft.pdfmu.jackson.SignatureMetadata;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import cz.hobrasoft.pdfmu.operation.metadata.MetadataParameters;
import cz.hobrasoft.pdfmu.operation.version.PdfVersion;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import javax.security.auth.x500.X500Principal;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Filip Bartek
 */
public class OperationInspect extends OperationCommon {

    private final InPdfArgs in = new InPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Display PDF version, properties and signatures of a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true);

        in.addArguments(subparser);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        in.setFromNamespace(namespace);

        Inspect result = new Inspect();

        in.open();
        PdfReader pdfReader = in.getPdfReader();

        // Fetch the PDF version of the input PDF document
        PdfVersion inVersion = new PdfVersion(pdfReader.getPdfVersion());
        to.println(String.format("PDF version: %s", inVersion));
        result.version = inVersion.toString();

        result.properties = get(pdfReader);

        result.signatures = display(pdfReader);

        in.close();

        writeResult(result);
    }

    private SortedMap<String, String> get(PdfReader pdfReader) {
        Map<String, String> properties = pdfReader.getInfo();

        MetadataParameters mp = new MetadataParameters();
        mp.setFromInfo(properties);

        SortedMap<String, String> propertiesSorted = mp.getSorted();

        {
            to.indentMore("Properties:");
            for (Map.Entry<String, String> property : propertiesSorted.entrySet()) {
                String key = property.getKey();
                String value = property.getValue();
                to.println(String.format("%s: %s", key, value));
            }
            to.indentLess();
        }

        return propertiesSorted;
    }

    public SignatureDisplay display(PdfReader pdfReader) {
        // digitalsignatures20130304.pdf : Code sample 5.1
        AcroFields fields = pdfReader.getAcroFields();
        return display(fields);
    }

    private SignatureDisplay display(AcroFields fields) {
        SignatureDisplay result = new SignatureDisplay();

        // digitalsignatures20130304.pdf : Code sample 5.1
        ArrayList<String> names = fields.getSignatureNames();

        // Print number of signatures
        to.println(String.format("Number of signatures: %d", names.size()));
        to.println(String.format("Number of document revisions: %d", fields.getTotalRevisions()));
        result.nRevisions = fields.getTotalRevisions();

        List<Signature> signatures = new ArrayList<>();

        for (String name : names) {
            to.println(String.format("Signature field name: %s", name));

            to.indentMore();
            Signature signature;
            try {
                signature = display(fields, name); // May throw OperationException
            } finally {
                to.indentLess();
            }
            signature.id = name;
            signatures.add(signature);
        }

        result.signatures = signatures;

        return result;
    }

    private Signature display(AcroFields fields, String name) {
        // digitalsignatures20130304.pdf : Code sample 5.2
        to.println(String.format("Signature covers the whole document: %s", (fields.signatureCoversWholeDocument(name) ? "Yes" : "No")));
        to.println(String.format("Document revision: %d of %d", fields.getRevision(name), fields.getTotalRevisions()));

        PdfPKCS7 pkcs7 = fields.verifySignature(name);
        Signature signature = display(pkcs7);
        signature.coversWholeDocument = fields.signatureCoversWholeDocument(name);
        signature.revision = fields.getRevision(name);
        return signature;
    }

    private Signature display(PdfPKCS7 pkcs7) {
        Signature signature = new Signature();

        // digitalsignatures20130304.pdf : Code sample 5.3
        to.println("Signature metadata:");
        {
            SignatureMetadata metadata = new SignatureMetadata();

            to.indentMore();

            // Only name may be null.
            // The values are set in {@link PdfPKCS7#verifySignature}.
            { // name
                String name = pkcs7.getSignName(); // May be null
                metadata.name = name;
                if (name == null) {
                    to.println("Name is not set.");
                } else {
                    to.println(String.format("Name: %s", name));
                }
            }

            // TODO?: Print "N/A" if the value is an empty string
            // TODO?: Determine whether the value is set in the signature
            to.println(String.format("Reason: %s", pkcs7.getReason()));
            metadata.reason = pkcs7.getReason();
            to.println(String.format("Location: %s", pkcs7.getLocation()));
            metadata.location = pkcs7.getLocation();

            { // Date
                Date date = pkcs7.getSignDate().getTime();
                to.println(String.format("Date and time: %s", date));
                metadata.date = date.toString();
            }

            to.indentLess();

            signature.metadata = metadata;
        }
        { // Certificate chain
            to.indentMore("Certificate chain:");
            Certificate[] certificates = pkcs7.getSignCertificateChain();
            to.println(String.format("Number of certificates: %d", certificates.length));
            int i = 0;
            List<CertificateResult> certificatesResult = new ArrayList<>();
            for (Certificate certificate : certificates) {
                to.indentMore(String.format("Certificate %d%s:", i, (i == 0 ? " (the signing certificate)" : "")));
                CertificateResult certRes;
                String type = certificate.getType();
                to.println(String.format("Type: %s", type));
                // http://docs.oracle.com/javase/1.5.0/docs/guide/security/CryptoSpec.html#AppA
                if ("X.509".equals(type)) {
                    X509Certificate certificateX509 = (X509Certificate) certificate;
                    certRes = showCertInfo(certificateX509);
                } else {
                    certRes = new CertificateResult();
                }
                certRes.type = type;
                to.indentLess();
                certificatesResult.add(certRes);
                ++i;
            }
            signature.certificates = certificatesResult;
            to.indentLess();
        }

        return signature;
    }

    private CertificateResult showCertInfo(X509Certificate cert) {
        CertificateResult certRes = new CertificateResult();

        { // Self-signed?
            X500Principal principalSubject = cert.getSubjectX500Principal();
            X500Principal principalIssuer = cert.getIssuerX500Principal();
            boolean selfSigned = principalSubject.equals(principalIssuer);
            to.println(String.format("Self-signed: %s", (selfSigned ? "Yes" : "No")));
            certRes.selfSigned = selfSigned;
        }

        // Note: More attributes may be available by more direct processing of `cert`
        // than by using `CertificateInfo.get*Fields`.
        { // Subject
            to.indentMore("Subject:");
            certRes.subject = showX500Name(CertificateInfo.getSubjectFields(cert));
            to.indentLess();
        }
        { // Issuer
            to.indentMore("Issuer:");
            certRes.issuer = showX500Name(CertificateInfo.getIssuerFields(cert));
            to.indentLess();
        }

        return certRes;
    }

    // The desired order of DN attributes by their type
    private static final MapSorter<String> dnTypeSorter = new PreferenceListComparator(new String[]{
        "CN", "E", "OU", "O", "STREET", "L", "ST", "C"});

    /**
     * The returned map is ordered by keys by {@link dnTypeSorter}.
     */
    private SortedMap<String, List<String>> showX500Name(X500Name name) {
        Map<String, ArrayList<String>> fields = name.getFields();

        // Convert to Map<String, List<String>>
        Map<String, List<String>> fieldsLists = new LinkedHashMap<>();
        fieldsLists.putAll(fields);

        // Sort by dnTypeSorter
        SortedMap<String, List<String>> fieldsSorted = dnTypeSorter.sort(fieldsLists);

        // Print
        for (Entry<String, List<String>> field : fieldsSorted.entrySet()) {
            String type = field.getKey();
            type = niceX500AttributeType(type);
            List<String> values = field.getValue();
            String valuesString = StringUtils.join(values, ", ");
            to.println(String.format("%s: %s", type, valuesString));
        }

        return fieldsSorted;
    }

    private static final Map<String, String> attributeTypeAliases = new HashMap<>();

    static {
        // Alias sources:
        // http://www.ietf.org/rfc/rfc2253.txt : Section 2.3
        // http://api.itextpdf.com/itext/com/itextpdf/text/pdf/security/CertificateInfo.X500Name.html
        attributeTypeAliases.put("CN", "Common name");
        attributeTypeAliases.put("L", "Locality");
        attributeTypeAliases.put("ST", "State or province");
        attributeTypeAliases.put("O", "Organization");
        attributeTypeAliases.put("OU", "Organizational unit");
        attributeTypeAliases.put("C", "Country code");
        attributeTypeAliases.put("STREET", "Street address");
        attributeTypeAliases.put("DC", "Domain component");
        attributeTypeAliases.put("UID", "User ID");
        attributeTypeAliases.put("E", "Email address");
        attributeTypeAliases.put("SN", "Serial number");
        attributeTypeAliases.put("T", "Title");
    }

    private static String niceX500AttributeType(String type) {
        String nice = attributeTypeAliases.get(type);
        if (nice != null) {
            type = nice;
        } else {
            return String.format("<%s>", type);
        }

        return type;
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationInspect();
        }
        return instance;
    }

    private OperationInspect() {
        // Singleton
    }

}
