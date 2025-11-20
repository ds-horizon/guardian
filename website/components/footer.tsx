export default function Footer() {
  return (
    <footer className="w-full border-t border-border/30 bg-background/95 backdrop-blur-sm">
      <div className="container max-w-[1600px] px-4 py-6 md:px-6">
        <div className="flex flex-col items-center justify-center text-center">
          <p className="text-xs text-muted-foreground">
            &copy; {new Date().getFullYear()} Guardian - MIT Licensed Open Source Project
          </p>
        </div>
      </div>
    </footer>
  )
}
