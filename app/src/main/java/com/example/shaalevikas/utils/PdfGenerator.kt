package com.example.shaalevikas.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.shaalevikas.model.Need
import com.example.shaalevikas.model.Pledge
import com.example.shaalevikas.ui.formatCurrency

object PdfGenerator {

    // Keep a reference to prevent garbage collection during generation
    private var activeWebView: WebView? = null

    fun generateProjectReport(
        context: Context,
        need: Need,
        donors: List<Pledge>
    ) {
        // Ensure this runs on the Main UI thread
        Handler(Looper.getMainLooper()).post {
            val webView = WebView(context)
            activeWebView = webView
            
            val donorRows = if (donors.isEmpty()) {
                "<tr><td colspan='2' style='text-align:center;'>No specific donors recorded for this project.</td></tr>"
            } else {
                donors.joinToString("") { 
                    "<tr><td>${it.donorName}</td><td>${formatCurrency(it.amount)}</td></tr>" 
                }
            }

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: sans-serif; padding: 20px; color: #333; }
                        .header { text-align: center; border-bottom: 3px solid #4CAF50; padding-bottom: 15px; margin-bottom: 30px; }
                        .section { margin-bottom: 25px; page-break-inside: avoid; }
                        .title { font-size: 28px; font-weight: bold; color: #2E7D32; margin-bottom: 5px; }
                        .meta { font-size: 14px; color: #666; margin-bottom: 20px; }
                        .photos { display: flex; justify-content: space-between; margin: 20px 0; }
                        .photo-box { width: 48%; text-align: center; }
                        .photo-box img { width: 100%; height: 220px; object-fit: cover; border: 2px solid #EEE; border-radius: 8px; }
                        .donor-list { border-collapse: collapse; width: 100%; margin-top: 10px; }
                        .donor-list th, .donor-list td { border: 1px solid #E0E0E0; padding: 12px; text-align: left; }
                        .donor-list th { background-color: #F5F5F5; font-weight: bold; color: #2E7D32; }
                        .description { line-height: 1.6; background: #F9F9F9; padding: 15px; border-radius: 8px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>Shaale Vikas</h1>
                        <p>Empowering Schools Through Alumni Connection</p>
                        <h2 style="color: #4CAF50;">Project Completion Report</h2>
                    </div>
                    
                    <div class="section">
                        <div class="title">${need.title}</div>
                        <div class="meta">
                            Location: ${need.schoolId} | 
                            Category: ${need.categories.joinToString(", ")} | 
                            Status: ${need.status}
                        </div>
                    </div>
                    
                    <div class="section">
                        <h3>The Challenge & Transformation</h3>
                        <div class="description">${need.description}</div>
                        <p><strong>Total Project Investment:</strong> ${formatCurrency(need.estimatedCost)}</p>
                    </div>
                    
                    <div class="section">
                        <h3>Visual Impact</h3>
                        <div class="photos">
                            <div class="photo-box">
                                <p><strong>Before</strong></p>
                                <img src="${need.imageUrlBefore}" alt="Before Photo">
                            </div>
                            <div class="photo-box">
                                <p><strong>After</strong></p>
                                <img src="${need.imageUrlAfter}" alt="After Photo">
                            </div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h3>Community Recognition</h3>
                        <p>We are deeply grateful to the following alumni whose contributions made this project possible:</p>
                        <table class="donor-list">
                            <thead>
                                <tr>
                                    <th>Donor Name</th>
                                    <th>Contribution</th>
                                </tr>
                            </thead>
                            <tbody>
                                $donorRows
                            </tbody>
                        </table>
                    </div>
                </body>
                </html>
            """.trimIndent()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "ShaaleVikas_Report_${need.title.replace(" ", "_")}"
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    
                    printManager.print(jobName, printAdapter, attributes)
                    
                    // Clear the reference after some delay to ensure printing starts
                    Handler(Looper.getMainLooper()).postDelayed({
                        activeWebView = null
                    }, 5000)
                }
            }

            webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        }
    }
}
