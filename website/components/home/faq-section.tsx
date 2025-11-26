"use client"

import { motion } from "framer-motion"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import SectionHeader from "@/components/ui/section-header"

const faqItems = [
  {
    question: "Is Guardian really 100% free?",
    answer:
      "Yes, Guardian is completely free and open source under the MIT license. You can use it in personal or commercial projects without any licensing fees. You only pay for your own infrastructure costs to host it.",
  },
  {
    question: "Can I contribute to Guardian?",
    answer:
      "Guardian is an open-source project and we welcome contributions from the community. Whether you're fixing bugs, adding features, or improving documentation, check out our contribution guidelines on GitHub to get started.",
  },
  {
    question: "What authentication methods does Guardian support?",
    answer:
      "Guardian supports a wide range of authentication methods including email/password, social logins (Google, Facebook), passwordless (SMS and Email otp).",
  },
  {
    question: "How do I get support for Guardian?",
    answer: "Guardian has a community on GitHub where you can open issues, ask questions, and get help.",
  },
]

export default function FaqSection() {
  return (
    <section id="faq" className="w-full py-12 md:py-20 bg-muted/30 relative overflow-hidden">
      <div className="absolute inset-0 -z-10 h-full w-full bg-black bg-[linear-gradient(to_right,#1f1f1f_1px,transparent_1px),linear-gradient(to_bottom,#1f1f1f_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_80%_50%_at_50%_50%,#000_40%,transparent_100%)]"></div>

      <div className="container max-w-[1600px] px-4 md:px-6 relative">
        <SectionHeader
          badge="FAQ"
          title="Frequently Asked Questions"
          description="Find answers to common questions about Guardian."
        />

        <div className="mx-auto max-w-3xl">
          <Accordion type="single" collapsible className="w-full">
            {faqItems.map((faq, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 10 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: i * 0.05 }}
              >
                <AccordionItem value={`item-${i}`} className="border-b border-border/40 py-2">
                  <AccordionTrigger className="text-left font-medium hover:no-underline">
                    {faq.question}
                  </AccordionTrigger>
                  <AccordionContent className="text-muted-foreground">{faq.answer}</AccordionContent>
                </AccordionItem>
              </motion.div>
            ))}
          </Accordion>
        </div>
      </div>
    </section>
  )
}
