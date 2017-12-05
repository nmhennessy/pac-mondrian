import java.awt.*;
import java.util.*;
import java.net.URL;
import javax.swing.*;

public class ArcadeWriter extends Hashtable
{
    
    Image[]     numbers = new Image[10];
    MediaTracker tracker;
    URL baseURL;
    Image a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
    JApplet pacMon;
    
    public ArcadeWriter (URL baseURL, JApplet pacMon)
    {
        super(36); //Initialize Hashtable with capacity of 8 entries.
        this.baseURL = baseURL;
        tracker = new MediaTracker(pacMon);
        for (int counter = 0; counter < 10; counter += 1) {
            numbers[counter] = pacMon.getImage(baseURL, Integer.toString(counter) + ".jpg");
            tracker.addImage(numbers[counter], 0);
            put(Integer.toString(counter), numbers[counter]);
        } //for
        a = pacMon.getImage(baseURL, "a.jpg");
        tracker.addImage(a, 0);
        put("a", a);
        b = pacMon.getImage(baseURL, "b.jpg");
        tracker.addImage(b, 0);
        put("b", b);
        c = pacMon.getImage(baseURL, "c.jpg");
        tracker.addImage(c, 0);
        put("c", c);
        d = pacMon.getImage(baseURL, "d.jpg");
        tracker.addImage(d, 0);
        put("d", d);
        e = pacMon.getImage(baseURL, "e.jpg");
        tracker.addImage(e, 0);
        put("e", e);
        f = pacMon.getImage(baseURL, "f.jpg");
        tracker.addImage(f, 0);
        put("f", f);
        g = pacMon.getImage(baseURL, "g.jpg");
        tracker.addImage(g, 0);
        put("g", g);
        h = pacMon.getImage(baseURL, "h.jpg");
        tracker.addImage(h, 0);
        put("h", h);
        i = pacMon.getImage(baseURL, "i.jpg");
        tracker.addImage(i, 0);
        put("i", i);
        j = pacMon.getImage(baseURL, "j.jpg");
        tracker.addImage(j, 0);
        put("j", j);
        k = pacMon.getImage(baseURL, "k.jpg");
        tracker.addImage(k, 0);
        put("k", k);
        l = pacMon.getImage(baseURL, "l.jpg");
        tracker.addImage(l, 0);
        put("l", l);
        m = pacMon.getImage(baseURL, "m.jpg");
        tracker.addImage(m, 0);
        put("m", m);
        n = pacMon.getImage(baseURL, "n.jpg");
        tracker.addImage(n, 0);
        put("n", n);
        o = pacMon.getImage(baseURL, "o.jpg");
        tracker.addImage(o, 0);
        put("o", o);
        p = pacMon.getImage(baseURL, "p.jpg");
        tracker.addImage(p, 0);
        put("p", p);
        q = pacMon.getImage(baseURL, "q.jpg");
        tracker.addImage(q, 0);
        put("q", q);
        r = pacMon.getImage(baseURL, "r.jpg");
        tracker.addImage(r, 0);
        put("r", r);
        s = pacMon.getImage(baseURL, "s.jpg");
        tracker.addImage(s, 0);
        put("s", s);
        t = pacMon.getImage(baseURL, "t.jpg");
        tracker.addImage(t, 0);
        put("t", t);
        u = pacMon.getImage(baseURL, "u.jpg");
        tracker.addImage(u, 0);
        put("u", u);
        v = pacMon.getImage(baseURL, "v.jpg");
        tracker.addImage(v, 0);
        put("v", v);
        w = pacMon.getImage(baseURL, "w.jpg");
        tracker.addImage(w, 0);
        put("w", w);
        x = pacMon.getImage(baseURL, "x.jpg");
        tracker.addImage(x, 0);
        put("x", x);
        y = pacMon.getImage(baseURL, "y.jpg");
        tracker.addImage(y, 0);
        put("y", y);
        z = pacMon.getImage(baseURL, "z.jpg");
        tracker.addImage(z, 0);
        put("z", z);
    }

    public void writeScore(int score, int xpos, int ypos, Graphics g, Canvas c)
    {
        if ((tracker.statusID(0, true) & MediaTracker.COMPLETE) == 0)
            return;
	if (score == 0)
	{
	    g.drawImage (numbers[0], xpos, ypos, c);
	} else {
	     while (score > 0)
	    {
		g.drawImage (numbers[score % 10], xpos, ypos, c);
        	score = (int) Math.floor(score/10);
		xpos -= 16;
	    }
	}
    }

    public void writeText(String text, int xpos, int ypos, Graphics g, Canvas c)
    {
        if ((tracker.statusID(0, true) & MediaTracker.COMPLETE) == 0)
            return;
        for (int count = 0; count < text.length(); count += 1)
        {
            if (text.substring(count, count + 1).equalsIgnoreCase(" "))
            {
                xpos += 16;
            } else {
                g.drawImage ((Image)get(text.substring(count, count + 1)), xpos, ypos, c);
                xpos += 16;
            }
        }
    }
    
}